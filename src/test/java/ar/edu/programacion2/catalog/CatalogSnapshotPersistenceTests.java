package ar.edu.programacion2.catalog;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotStore;
import ar.edu.programacion2.catalog.catalog.domain.model.*;
import ar.edu.programacion2.catalog.catalog.infrastructure.persistence.*;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogSnapshotPersistenceTests {
    @Container
    static final org.testcontainers.mysql.MySQLContainer MYSQL = new org.testcontainers.mysql.MySQLContainer("mysql:8.4.8");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", MYSQL::getJdbcUrl);
        properties.add("spring.datasource.username", MYSQL::getUsername);
        properties.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired CatalogSnapshotStore store;
    @Autowired ProfessionalCategoryRepository categories;
    @Autowired ProfessionalRepository professionals;
    @Autowired WeeklyScheduleRepository schedules;
    @Autowired CatalogSyncStateRepository states;
    @Autowired JdbcTemplate jdbc;
    private static final Instant SOURCE_TIME = Instant.parse("2026-09-10T13:40:00Z");

    @BeforeEach
    void resetDatabase() {
        schedules.deleteAllInBatch();
        professionals.deleteAllInBatch();
        categories.deleteAllInBatch();
        jdbc.update("DELETE FROM catalog_sync_state");
        jdbc.update("INSERT INTO catalog_sync_state VALUES (1, NULL, NULL)");
    }

    @Test
    void appliesVersionZeroWithSourceIdsDisabledRecordsAndLocalTimes() {
        assertThat(store.apply(snapshot(0, "Clínica"))).isTrue();
        assertThat(categories.findById(101L).orElseThrow().getEnabled()).isFalse();
        assertThat(professionals.findById(201L).orElseThrow().getCategoryId()).isEqualTo(101L);
        var schedule = schedules.findById(301L).orElseThrow();
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.getCreatedAt()).isEqualTo(SOURCE_TIME);
        var state = states.findById(1).orElseThrow();
        assertThat(state.getAppliedVersion()).isZero();
        assertThat(state.getAppliedAt()).isAfter(SOURCE_TIME);
    }

    @Test
    void replacesExistingIdsAndRemovesRecordsAbsentFromNewSnapshot() {
        store.apply(snapshot(1, "Anterior"));
        var replacement = new CatalogSnapshot(2, SOURCE_TIME,
            List.of(category("Actualizada")), List.of(), List.of());
        assertThat(store.apply(replacement)).isTrue();
        assertThat(categories.findAll()).singleElement()
            .satisfies(category -> assertThat(category.getName()).isEqualTo("Actualizada"));
        assertThat(professionals.count()).isZero();
        assertThat(schedules.count()).isZero();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(2);
    }

    @Test
    void ignoresRepeatedAndOlderVersionsWithoutChangingDataOrApplicationTime() {
        store.apply(snapshot(7, "Original"));
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        assertThat(store.apply(snapshot(7, "Duplicada"))).isFalse();
        assertThat(store.apply(snapshot(6, "Antigua"))).isFalse();
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(7);
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
    }

    @Test
    void emptySnapshotClearsAllCollectionsAndAdvancesVersion() {
        store.apply(snapshot(1, "Original"));
        assertThat(store.apply(new CatalogSnapshot(2, SOURCE_TIME, List.of(), List.of(), List.of()))).isTrue();
        assertThat(categories.count()).isZero();
        assertThat(professionals.count()).isZero();
        assertThat(schedules.count()).isZero();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(2);
    }

    @Test
    void databaseFailureRestoresAllPreviousDataAndVersion() {
        store.apply(snapshot(1, "Original"));
        var appliedAt = states.findById(1).orElseThrow().getAppliedAt();
        var invalidProfessional = new Professional(201L, 101L, "x".repeat(70000), "Gómez",
            true, SOURCE_TIME, SOURCE_TIME);
        var invalid = new CatalogSnapshot(2, SOURCE_TIME, List.of(category("Nueva")),
            List.of(invalidProfessional), List.of());
        // Supera el límite TEXT de MySQL después de borrar la copia anterior.
        assertThatThrownBy(() -> store.apply(invalid)).isInstanceOf(DataAccessException.class);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
        assertThat(professionals.findById(201L).orElseThrow().getFirstName()).isEqualTo("Ana");
        assertThat(schedules.findById(301L)).isPresent();
        assertThat(states.findById(1).orElseThrow().getAppliedVersion()).isEqualTo(1);
        assertThat(states.findById(1).orElseThrow().getAppliedAt()).isEqualTo(appliedAt);
    }

    @Test
    void missingStateAndNullSnapshotFailBeforeReplacingData() {
        store.apply(snapshot(1, "Original"));
        assertThatNullPointerException().isThrownBy(() -> store.apply(null));
        jdbc.update("DELETE FROM catalog_sync_state");
        assertThatThrownBy(() -> store.apply(snapshot(2, "Nueva")))
            .isInstanceOf(DataAccessException.class).hasRootCauseInstanceOf(IllegalStateException.class);
        assertThat(categories.findById(101L).orElseThrow().getName()).isEqualTo("Original");
    }

    private static ProfessionalCategory category(String name) {
        return new ProfessionalCategory(101L, name, null, false, SOURCE_TIME, SOURCE_TIME);
    }

    private static CatalogSnapshot snapshot(long version, String name) {
        return new CatalogSnapshot(version, SOURCE_TIME, List.of(category(name)),
            List.of(new Professional(201L, 101L, "Ana", "Gómez", false, SOURCE_TIME, SOURCE_TIME)),
            List.of(new WeeklySchedule(301L, 201L, DayOfWeek.MONDAY, LocalTime.of(9, 0),
                LocalTime.of(12, 0), 30, false, SOURCE_TIME, SOURCE_TIME)));
    }
}
