package ma.karflow.feature.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.karflow.feature.auth.dto.*;
import ma.karflow.feature.auth.entity.PasswordResetToken;
import ma.karflow.feature.auth.entity.Permission;
import ma.karflow.feature.auth.entity.Role;
import ma.karflow.feature.auth.entity.User;
import ma.karflow.feature.auth.enums.RoleType;
import ma.karflow.feature.auth.repository.PasswordResetTokenRepository;
import ma.karflow.feature.auth.repository.PermissionRepository;
import ma.karflow.feature.auth.repository.RoleRepository;
import ma.karflow.feature.auth.repository.UserRepository;
import ma.karflow.feature.auth.security.JwtService;
import ma.karflow.feature.auth.security.UserDetailsAdapter;
import ma.karflow.feature.organisation.entity.Organisation;
import ma.karflow.feature.organisation.repository.OrganisationRepository;
import ma.karflow.feature.subscription.entity.Subscription;
import ma.karflow.feature.subscription.enums.SubscriptionPlan;
import ma.karflow.feature.subscription.enums.SubscriptionStatus;
import ma.karflow.feature.subscription.repository.SubscriptionRepository;
import ma.karflow.shared.exception.BusinessException;
import ma.karflow.shared.exception.DuplicateResourceException;
import ma.karflow.shared.exception.ResourceNotFoundException;
import ma.karflow.shared.util.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final OrganisationRepository organisationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${karflow.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    private static final List<String> DEFAULT_PERMISSIONS = List.of(
            "VEHICLE_READ", "VEHICLE_WRITE",
            "CLIENT_READ", "CLIENT_WRITE",
            "RENTAL_MANAGE",
            "INVOICE_MANAGE",
            "DASHBOARD_VIEW",
            "SETTINGS_MANAGE"
    );

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Generate a tenantId for the new organisation
        UUID tenantId = UUID.randomUUID();

        // Check email uniqueness within tenant
        if (userRepository.existsByEmailAndTenantId(request.email(), tenantId)) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        // Create default permissions for this tenant
        List<Permission> permissions = DEFAULT_PERMISSIONS.stream()
                .map(permName -> {
                    Permission perm = new Permission(permName);
                    perm.setTenantId(tenantId);
                    return perm;
                })
                .toList();
        permissions = permissionRepository.saveAll(permissions);

        // Create OWNER role with all permissions
        Role ownerRole = new Role(RoleType.OWNER);
        ownerRole.setTenantId(tenantId);
        ownerRole.setPermissions(new HashSet<>(permissions));
        ownerRole = roleRepository.save(ownerRole);

        // Create ADMIN role with all permissions except SETTINGS_MANAGE
        Role adminRole = new Role(RoleType.ADMIN);
        adminRole.setTenantId(tenantId);
        adminRole.setPermissions(permissions.stream()
                .filter(p -> !"SETTINGS_MANAGE".equals(p.getName()))
                .collect(Collectors.toSet()));
        roleRepository.save(adminRole);

        // Create AGENT role with limited permissions
        Role agentRole = new Role(RoleType.AGENT);
        agentRole.setTenantId(tenantId);
        agentRole.setPermissions(permissions.stream()
                .filter(p -> Set.of("VEHICLE_READ", "CLIENT_READ", "CLIENT_WRITE", "RENTAL_MANAGE")
                        .contains(p.getName()))
                .collect(Collectors.toSet()));
        roleRepository.save(agentRole);

        // Create the organisation for this tenant
        Organisation organisation = new Organisation();
        organisation.setTenantId(tenantId);
        organisation.setName(request.organisationName());
        organisation.setEmail(request.email());
        organisation = organisationRepository.save(organisation);

        // Create TRIAL subscription for the new organisation
        Subscription subscription = new Subscription();
        subscription.setTenantId(tenantId);
        subscription.setOrganisation(organisation);
        subscription.setPlan(SubscriptionPlan.TRIAL);
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setTrialStartDate(LocalDateTime.now());
        subscription.setTrialEndDate(LocalDateTime.now().plusDays(20));
        subscription.setMonthlyPrice(BigDecimal.ZERO);
        subscription.setAutoRenew(true);
        subscriptionRepository.save(subscription);

        // Create the user with OWNER role
        User user = new User();
        user.setTenantId(tenantId);
        user.setOrganisationId(organisation.getId());
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setEnabled(true);
        user.setRoles(Set.of(ownerRole));
        user = userRepository.save(user);

        log.info("New tenant registered: {} (tenantId: {})", request.email(), tenantId);

        // Send welcome email
        emailService.sendHtmlEmail(
                request.email(),
                "Bienvenue sur KarFlow",
                "welcome",
                Map.of("username", request.username(), "organisationName", request.organisationName())
        );

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        if (!user.isEnabled()) {
            throw new BusinessException("Ce compte est désactivé");
        }

        log.info("User logged in: {} (tenantId: {})", user.getEmail(), user.getTenantId());

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtService.isTokenValid(request.refreshToken())) {
            throw new BusinessException("Refresh token invalide ou expiré");
        }

        String email = jwtService.extractEmail(request.refreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsAdapter userDetails)) {
            throw new BusinessException("Utilisateur non authentifié");
        }

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", userDetails.getUserId()));

        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getPhotoUrl(),
                user.isEnabled(),
                user.getOrganisationId(),
                roleNames,
                user.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        // Always return success to avoid email enumeration
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", request.email());
            return;
        }

        // Cleanup old unused tokens for this user
        passwordResetTokenRepository.deleteAllByUserIdAndUsedFalse(user.getId());

        // Generate token and save
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setTenantId(user.getTenantId());
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        passwordResetTokenRepository.save(resetToken);

        // Build reset link and send email
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        emailService.sendHtmlEmail(
                user.getEmail(),
                "Réinitialisation de votre mot de passe KarFlow",
                "password-reset",
                Map.of("username", user.getUsername(), "resetUrl", resetUrl)
        );

        log.info("Password reset token generated for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUsedFalse(request.token())
                .orElseThrow(() -> new BusinessException("Token de réinitialisation invalide ou déjà utilisé"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Le token de réinitialisation a expiré");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    private AuthResponse buildAuthResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        String accessToken = jwtService.generateAccessToken(
                user.getEmail(), user.getId(), user.getTenantId(), roles, permissions
        );
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getTenantId(),
                roles,
                permissions
        );
    }
}
