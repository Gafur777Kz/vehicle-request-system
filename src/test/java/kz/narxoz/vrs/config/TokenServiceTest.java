package kz.narxoz.vrs.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// без Spring и без веба: TokenService подписывает токен, JwtDecoder с тем же секретом его читает
class TokenServiceTest {

    private static final SecretKey KEY = key("test-secret-at-least-32-bytes-long!!");

    private static SecretKey key(String secret) {
        return new SecretKeySpec(secret.getBytes(UTF_8), "HmacSHA256");
    }

    private static String managerToken() {
        TokenService tokens = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(KEY)));
        return tokens.issue(UsernamePasswordAuthenticationToken.authenticated(
                "manager", null, List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))));
    }

    @Test
    void issuedTokenCarriesUserRoleAndExpiry() {
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(KEY).macAlgorithm(MacAlgorithm.HS256).build();

        Jwt jwt = decoder.decode(managerToken());

        assertEquals("manager", jwt.getSubject());
        assertEquals(List.of("MANAGER"), jwt.getClaimAsStringList("roles"));   // без ROLE_
        assertEquals("vehicle-request-system", jwt.getClaimAsString("iss"));
        assertTrue(jwt.getExpiresAt().isAfter(Instant.now()));
        assertEquals(Duration.ofMinutes(15), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
    }

    // чужой секрет -> подпись не сходится -> токен не принимается (в API это 401)
    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtDecoder otherDecoder = NimbusJwtDecoder.withSecretKey(key("another-secret-also-32-bytes-long!!!"))
                .macAlgorithm(MacAlgorithm.HS256).build();

        assertThrows(JwtException.class, () -> otherDecoder.decode(managerToken()));
    }

    // короткий секрет не принимается при старте
    @Test
    void shortSecretFails() {
        assertThrows(IllegalStateException.class, () -> new JwtConfig().jwtKey("secret"));
    }
}
