package io.pathops.controlplane;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class PathopsControlPlaneApplication {

    public static void main(@NonNull String[] args) {
        SpringApplication.run(PathopsControlPlaneApplication.class, args);
    }
}