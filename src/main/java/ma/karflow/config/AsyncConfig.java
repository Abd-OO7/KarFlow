package ma.karflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Active le support @Async pour l'envoi d'emails non-bloquant.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
