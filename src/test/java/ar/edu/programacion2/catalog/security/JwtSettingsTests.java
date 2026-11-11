package ar.edu.programacion2.catalog.security;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class JwtSettingsTests {
    @ParameterizedTest @ValueSource(strings={"","not-base64!","YWJj"})
    void rejectsInvalidKeyWithoutLeakingIt(String key) {
        assertThatThrownBy(() -> new JwtSettings(key,"issuer","audience"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageStartingWith("Invalid catalog JWT configuration");
    }
    @Test void validatesKeySizeAndRequiredSettings() {
        String shortKey=Base64.getEncoder().encodeToString(new byte[31]);
        assertThatThrownBy(() -> new JwtSettings(shortKey,"issuer","audience")).isInstanceOf(IllegalArgumentException.class);
        String key=Base64.getEncoder().encodeToString(new byte[32]);
        assertThatThrownBy(() -> new JwtSettings(key," ","audience")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtSettings(key,"issuer"," ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new JwtSettings(null,"issuer","audience")).isInstanceOf(IllegalArgumentException.class);
        var settings=new JwtSettings(key,"turnos-booking","turnos-backends");
        assertThat(settings.key().getEncoded()).hasSize(32);
        assertThat(settings.issuer()).isEqualTo("turnos-booking");
        assertThat(settings.audience()).isEqualTo("turnos-backends");
    }
    @Test void nonWebSynchronizationDoesNotRequireJwtKey() {
        new ApplicationContextRunner().withUserConfiguration(SecurityConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed(); assertThat(context).doesNotHaveBean(JwtSettings.class);
        });
    }
}
