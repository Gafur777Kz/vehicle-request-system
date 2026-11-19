package kz.narxoz.vrs.handler;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import kz.narxoz.vrs.config.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// логин: проверяем имя и BCrypt-хеш, выдаём JWT на 15 минут
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager auth;
    private final TokenService tokens;

    public AuthController(AuthenticationManager auth, TokenService tokens) {
        this.auth = auth;
        this.tokens = tokens;
    }

    // неверный пароль -> BadCredentialsException -> ApiErrorHandler отдаёт 401
    @PostMapping("/login")
    @ApiResponse(responseCode = "200", description = "токен выдан",
            content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @ApiResponse(responseCode = "400", description = "VALIDATION - пустое имя или пароль",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED - bad username or password",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public TokenResponse login(@Valid @RequestBody LoginRequest body) {
        Authentication user = auth.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(body.username(), body.password()));
        return new TokenResponse(tokens.issue(user), "Bearer", TokenService.LIFETIME.toSeconds());
    }
}
