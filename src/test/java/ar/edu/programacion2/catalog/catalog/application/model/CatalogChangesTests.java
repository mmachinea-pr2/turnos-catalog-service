package ar.edu.programacion2.catalog.catalog.application.model;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CatalogChangesTests {
    @Test
    void defensivelyCopiesEachCollection() {
        var ids = new ArrayList<>(List.of(1L));
        var changes = new CatalogChanges(7, ids, ids, ids);
        ids.clear();
        assertThat(changes.professionalCategories()).containsExactly(1L);
        assertThat(changes.professionals()).containsExactly(1L);
        assertThat(changes.weeklySchedules()).containsExactly(1L);
        assertThatThrownBy(() -> changes.professionals().add(2L)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void validatesAllCollectionsAndVersion() {
        assertThatIllegalArgumentException().isThrownBy(() -> new CatalogChanges(-1, List.of(), List.of(), List.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> new CatalogChanges(7, List.of(0L), List.of(), List.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> new CatalogChanges(7, List.of(), List.of(), List.of(1L,1L)));
        assertThatNullPointerException().isThrownBy(() -> new CatalogChanges(7, null, List.of(), List.of()));
    }
}
