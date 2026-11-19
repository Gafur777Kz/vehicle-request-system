package kz.narxoz.vrs.handler;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

// любое тело ошибки: только эти три поля
public record ApiError(String code, String message, String correlationId) {

    // берём X-Correlation-Id клиента, а если его нет - делаем новый
    public static String correlationId(HttpServletRequest request) {
        String id = request.getHeader("X-Correlation-Id");
        if (id == null || id.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return id;
    }
}
