package ar.edu.programacion2.catalog.catalog.infrastructure.rest;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotSource;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class CatalogRestConfigurationTests {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(CatalogRestConfiguration.class);

    @Test
    void disabledByDefaultWithoutCredentials() {
        runner.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(CatalogSnapshotSource.class));
    }

    @Test
    void enabledConfigurationCreatesClientWithoutCallingServer() {
        runner.withPropertyValues("catalog.catedra.enabled=true",
            "catalog.catedra.base-url=http://127.0.0.1:1", "catalog.catedra.jwt=test-token",
            "catalog.catedra.connect-timeout=5s", "catalog.catedra.read-timeout=10s")
            .run(context -> assertThat(context).hasNotFailed().hasSingleBean(CatalogSnapshotSource.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"file:///tmp/test", "http://user:pass@example.test", "http://example.test?token=x", "http://example.test#x", "http:/missing-host"})
    void rejectsUnsafeOrInvalidBaseUrl(String url) {
        assertThatIllegalArgumentException().isThrownBy(() -> create(url, "test-token", Duration.ofSeconds(1), Duration.ofSeconds(1)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "token\rvalue", "token\nvalue"})
    void rejectsEmptyOrMultilineToken(String token) {
        assertThatIllegalArgumentException().isThrownBy(() -> create("http://example.test", token, Duration.ofSeconds(1), Duration.ofSeconds(1)));
    }

    @Test
    void rejectsUnlimitedOrNegativeTimeouts() {
        assertThatIllegalArgumentException().isThrownBy(() -> create("http://example.test", "test", Duration.ZERO, Duration.ofSeconds(1)));
        assertThatIllegalArgumentException().isThrownBy(() -> create("http://example.test", "test", Duration.ofSeconds(-1), Duration.ofSeconds(1)));
        assertThatIllegalArgumentException().isThrownBy(() -> create("http://example.test", "test", Duration.ofSeconds(1), Duration.ZERO));
        assertThatIllegalArgumentException().isThrownBy(() -> create("http://example.test", "test", Duration.ofSeconds(1), Duration.ofSeconds(-1)));
    }

    private CatalogSnapshotSource create(String url, String token, Duration connect, Duration read) {
        return new CatalogRestConfiguration().catalogSnapshotSource(URI.create(url), token, connect.toString(), read.toString());
    }
}
