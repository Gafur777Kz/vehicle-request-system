package kz.narxoz.vrs.config;

import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// Application лежит в config, поэтому сканируем весь kz.narxoz.vrs (persistence, client).
@SpringBootApplication(scanBasePackages = "kz.narxoz.vrs")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner demo(VehicleRequestService service) {
        return args -> {
            VehicleRequestStatus status = service.move(
                    VehicleRequestId.newId(), VehicleRequestStatus.DRAFT, VehicleRequestStatus.SUBMITTED);
            System.out.println("Vehicle request moved: DRAFT -> " + status);
        };
    }
}
