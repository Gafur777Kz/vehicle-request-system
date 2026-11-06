package kz.narxoz.vrs.handler;

// любое тело ошибки: только эти три поля
public record ApiError(String code, String message, String correlationId) {
}
