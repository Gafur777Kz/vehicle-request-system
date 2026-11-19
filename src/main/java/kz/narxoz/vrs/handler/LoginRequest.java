package kz.narxoz.vrs.handler;

import jakarta.validation.constraints.NotBlank;

// тело POST /api/v1/auth/login. Пароль приходит только сюда
public record LoginRequest(
        @NotBlank(message = "must not be blank") String username,
        @NotBlank(message = "must not be blank") String password) {
}
