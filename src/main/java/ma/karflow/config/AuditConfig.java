package ma.karflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Active l'audit JPA pour les annotations @CreatedDate / @LastModifiedDate.
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {
}
