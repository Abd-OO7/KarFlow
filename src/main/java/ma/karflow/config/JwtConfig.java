package ma.karflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propriétés JWT externalisées.
 * Implémenté complètement en F-02.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "karflow.jwt")
public class JwtConfig {

    private String secret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
}
