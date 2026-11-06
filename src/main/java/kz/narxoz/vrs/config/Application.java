package kz.narxoz.vrs.config;

import kz.narxoz.vrs.client.DownstreamProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

// Application лежит в config, поэтому указываем пакеты явно
@SpringBootApplication(scanBasePackages = "kz.narxoz.vrs")
@EntityScan("kz.narxoz.vrs.persistence")
@EnableConfigurationProperties(DownstreamProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
