package kz.narxoz.vrs.handler;

// ответ логина: сам токен, тип Bearer и срок жизни в секундах (900 = 15 минут)
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
}
