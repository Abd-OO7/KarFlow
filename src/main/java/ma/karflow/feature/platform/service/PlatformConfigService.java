package ma.karflow.feature.platform.service;

import lombok.RequiredArgsConstructor;
import ma.karflow.feature.platform.entity.PlatformConfig;
import ma.karflow.feature.platform.repository.PlatformConfigRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformConfigService {

    private final PlatformConfigRepository configRepository;

    public List<PlatformConfig> getAllConfigs() {
        return configRepository.findAllByOrderByConfigKeyAsc();
    }

    public Map<String, String> getAllConfigsAsMap() {
        return configRepository.findAll().stream()
                .collect(Collectors.toMap(PlatformConfig::getConfigKey, PlatformConfig::getConfigValue));
    }

    public String getValue(String key, String defaultValue) {
        return configRepository.findByConfigKey(key)
                .map(PlatformConfig::getConfigValue)
                .orElse(defaultValue);
    }

    public int getTrialDurationDays() {
        return Integer.parseInt(getValue("trial.duration.days", "20"));
    }

    public BigDecimal getProMonthlyPrice() {
        return new BigDecimal(getValue("plan.pro.monthly.price", "299.00"));
    }

    public BigDecimal getMaxMonthlyPrice() {
        return new BigDecimal(getValue("plan.max.monthly.price", "599.00"));
    }

    public int getTrialReminderDays() {
        return Integer.parseInt(getValue("trial.reminder.days", "5"));
    }

    public int getGracePeriodDays() {
        return Integer.parseInt(getValue("grace.period.days", "7"));
    }

    public void updateConfig(String key, String value) {
        PlatformConfig config = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Config key not found: " + key));
        config.setConfigValue(value);
        configRepository.save(config);
    }

    public void updateConfigs(Map<String, String> updates) {
        updates.forEach(this::updateConfig);
    }
}
