package ar.edu.programacion2.catalog.security;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
@Configuration(proxyBeanMethods=false)
@ConditionalOnWebApplication(type=ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfiguration {
    @Bean JwtSettings catalogJwtSettings(@Value("${catalog.security.secret:}") String secret,
            @Value("${catalog.security.issuer:turnos-booking}") String issuer,
            @Value("${catalog.security.audience:turnos-backends}") String audience) {
        return new JwtSettings(secret,issuer,audience);
    }
    @Bean JwtDecoder jwtDecoder(JwtSettings settings) {
        var decoder = NimbusJwtDecoder.withSecretKey(settings.key()).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(Duration.ZERO),new JwtIssuerValidator(settings.issuer()),jwt -> {
                boolean valid = validSubject(jwt.getSubject()) && jwt.getAudience() != null
                    && jwt.getAudience().contains(settings.audience()) && validDates(jwt)
                    && (jwt.getClaim("auth") == null || jwt.getClaim("auth") instanceof String);
                return valid ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token","Invalid token claims",null));
            }));
        return decoder;
    }
    private boolean validSubject(String subject) {
        try { return UUID.fromString(subject).toString().equals(subject); }
        catch (IllegalArgumentException | NullPointerException exception) { return false; }
    }
    private boolean validDates(Jwt jwt) {
        return jwt.getExpiresAt() != null && jwt.getIssuedAt() != null
            && jwt.getExpiresAt().isAfter(jwt.getIssuedAt()) && !jwt.getIssuedAt().isAfter(Instant.now());
    }
    @Bean SecurityFilterChain catalogSecurity(HttpSecurity http) throws Exception {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("auth");
        authorities.setAuthorityPrefix("");
        var authentication = new JwtAuthenticationConverter();
        authentication.setJwtGrantedAuthoritiesConverter(authorities);
        return http.csrf(withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(access -> access
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET,"/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/catalog/**").hasRole("USER")
                .anyRequest().denyAll())
            .oauth2ResourceServer(resource -> resource.jwt(jwt -> jwt.jwtAuthenticationConverter(authentication)))
            .build();
    }
}
