package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationAction;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationStatus;
import ar.edu.programacion2.catalog.catalog.application.model.RemoteCatalogVersions;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionReadException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.*;

class DetermineCatalogSynchronizationServiceTests {
    @ParameterizedTest
    @CsvSource(value = {
        "null, 0, 0, FETCH_SNAPSHOT",
        "null, 7, 4, FETCH_SNAPSHOT",
        "0, 0, 0, UP_TO_DATE",
        "7, 7, 4, UP_TO_DATE",
        "3, 7, 4, FETCH_SNAPSHOT",
        "4, 7, 4, APPLY_INCREMENTS",
        "6, 7, 4, APPLY_INCREMENTS",
        "0, 1, 0, APPLY_INCREMENTS",
        "0, 1, 1, FETCH_SNAPSHOT",
        "8, 7, 4, LOCAL_VERSION_AHEAD",
        "9223372036854775807, 9223372036854775807, 0, UP_TO_DATE",
        "9223372036854775806, 9223372036854775807, 0, APPLY_INCREMENTS"
    }, nullValues = "null")
    void choosesActionAtVersionBoundaries(Long local, long current, long oldest,
            CatalogSynchronizationAction expected) {
        var service = new DetermineCatalogSynchronizationService(
            () -> new CatalogSynchronizationStatus(local, null),
            () -> new RemoteCatalogVersions(current, oldest));
        assertThat(service.determine()).isEqualTo(expected);
    }

    @Test
    void remoteFailureDoesNotPretendCatalogIsCurrent() {
        var failure = new CatalogVersionReadException();
        var service = new DetermineCatalogSynchronizationService(
            () -> new CatalogSynchronizationStatus(7L, null), () -> { throw failure; });
        assertThatThrownBy(service::determine).isSameAs(failure);
    }

    @Test
    void localReadFailureIsNotTreatedAsAnEmptyCatalog() {
        var failure = new IllegalStateException("Local state unavailable");
        var service = new DetermineCatalogSynchronizationService(() -> { throw failure; },
            () -> new RemoteCatalogVersions(7, 4));
        assertThatThrownBy(service::determine).isSameAs(failure);
    }

    @Test
    void rejectsNegativeLocalVersion() {
        var service = new DetermineCatalogSynchronizationService(
            () -> new CatalogSynchronizationStatus(-1L, null), () -> new RemoteCatalogVersions(7, 4));
        assertThatIllegalStateException().isThrownBy(service::determine);
    }
}
