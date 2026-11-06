package kz.narxoz.vrs.handler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// тело POST. purpose — это "title" из задания (цель поездки)
// текст сообщений задан явно, чтобы не зависеть от языка компьютера
public record CreateVehicleRequest(
        @NotBlank(message = "must not be blank") String businessKey,
        @NotBlank(message = "must not be blank")
        @Size(max = 120, message = "size must be between 0 and 120") String purpose) {
}
