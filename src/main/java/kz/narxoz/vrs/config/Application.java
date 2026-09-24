package kz.narxoz.vrs.config;

import kz.narxoz.vrs.domain.VehicleRequestStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner demo(VehicleRequestService service) {
        return args -> {
            VehicleRequestStatus status = service.move(VehicleRequestStatus.DRAFT, VehicleRequestStatus.SUBMITTED);
            System.out.println("Vehicle request moved: DRAFT -> " + status);
        };
    }
}
