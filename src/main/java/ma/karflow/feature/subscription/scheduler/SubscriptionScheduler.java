package ma.karflow.feature.subscription.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.subscription.entity.Subscription;
import ma.karflow.feature.subscription.entity.SubscriptionHistory;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;
import ma.karflow.feature.subscription.repository.SubscriptionHistoryRepository;
import ma.karflow.feature.subscription.repository.SubscriptionRepository;
import ma.karflow.shared.util.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final EmailService emailService;

    /**
     * Vérifie toutes les heures les essais expirés et met à jour leur statut.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void checkExpiredTrials() {
        List<Subscription> expiredTrials = subscriptionRepository.findAllByStatus(SubscriptionStatus.TRIAL)
                .stream()
                .filter(s -> s.getTrialEndDate().isBefore(LocalDateTime.now()))
                .toList();

        for (Subscription subscription : expiredTrials) {
            SubscriptionStatus previousStatus = subscription.getStatus();
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);

            // Create history entry
            SubscriptionHistory history = new SubscriptionHistory();
            history.setTenantId(subscription.getTenantId());
            history.setSubscription(subscription);
            history.setPreviousPlan(subscription.getPlan());
            history.setNewPlan(subscription.getPlan());
            history.setPreviousStatus(previousStatus);
            history.setNewStatus(SubscriptionStatus.EXPIRED);
            history.setChangeDate(LocalDateTime.now());
            history.setReason("Période d'essai expirée automatiquement");
            subscriptionHistoryRepository.save(history);

            // Send trial expired email
            String email = subscription.getOrganisation().getEmail();
            if (email != null && !email.isBlank()) {
                emailService.sendHtmlEmail(
                        email,
                        "Votre essai KarFlow est terminé",
                        "trial-expired",
                        Map.of(
                                "organisationName", subscription.getOrganisation().getName()
                        )
                );
            }

            log.info("Trial expired for tenant {} (organisation: {})",
                    subscription.getTenantId(), subscription.getOrganisation().getName());
        }

        if (!expiredTrials.isEmpty()) {
            log.info("Processed {} expired trials", expiredTrials.size());
        }
    }

    /**
     * Envoie des rappels quotidiens à 8h pour les essais expirant bientôt (5 jours et 1 jour avant).
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendTrialReminders() {
        LocalDateTime now = LocalDateTime.now();

        // Reminder 5 days before expiry
        sendRemindersForRange(now.plusDays(4).with(LocalTime.MIN), now.plusDays(5).with(LocalTime.MAX), 5);

        // Reminder 1 day before expiry
        sendRemindersForRange(now.with(LocalTime.MIN), now.plusDays(1).with(LocalTime.MAX), 1);
    }

    private void sendRemindersForRange(LocalDateTime from, LocalDateTime to, int daysRemaining) {
        List<Subscription> expiringTrials = subscriptionRepository.findTrialsExpiringBetween(from, to);

        for (Subscription subscription : expiringTrials) {
            String email = subscription.getOrganisation().getEmail();
            if (email != null && !email.isBlank()) {
                emailService.sendHtmlEmail(
                        email,
                        "Il vous reste " + daysRemaining + " jour(s) d'essai KarFlow",
                        "trial-reminder",
                        Map.of(
                                "organisationName", subscription.getOrganisation().getName(),
                                "daysRemaining", daysRemaining
                        )
                );
                log.info("Trial reminder sent to {} ({} days remaining)", email, daysRemaining);
            }
        }
    }
}
