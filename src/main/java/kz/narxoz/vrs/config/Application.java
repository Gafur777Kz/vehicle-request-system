package kz.narxoz.vrs.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

// Application лежит в config, поэтому указываем пакеты явно
@SpringBootApplication(scanBasePackages = "kz.narxoz.vrs")
@EntityScan("kz.narxoz.vrs.persistence")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
