package ma.karflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KarFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(KarFlowApplication.class, args);
    }
}
