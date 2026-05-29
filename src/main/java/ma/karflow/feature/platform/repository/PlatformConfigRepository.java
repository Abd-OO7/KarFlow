package ma.karflow.feature.platform.repository;

import ma.karflow.feature.platform.entity.PlatformConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, UUID> {

    Optional<PlatformConfig> findByConfigKey(String configKey);

    List<PlatformConfig> findAllByOrderByConfigKeyAsc();
}
