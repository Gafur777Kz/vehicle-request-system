package kz.narxoz.vrs.handler;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// кто я: имя и роли из проверенного токена (их положил туда фильтр)
@RestController
@SecurityRequirement(name = "bearer")
public class MeController {

    @GetMapping("/api/me")
    @ApiResponse(responseCode = "200", description = "имя и роли из токена")
    @ApiResponse(responseCode = "401", description = "UNAUTHENTICATED - нет токена",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public Map<String, Object> me(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Map.of("username", authentication.getName(), "authorities", authorities);
    }
}
