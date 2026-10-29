package ar.edu.programacion2.catalog;

import java.net.URI;
import java.time.Instant;
import java.time.LocalTime;
import java.time.DayOfWeek;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import ar.edu.programacion2.catalog.catalog.infrastructure.persistence.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogApplicationTests {
    @Container
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4.8");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", MYSQL::getJdbcUrl);
        properties.add("spring.datasource.username", MYSQL::getUsername);
        properties.add("spring.datasource.password", MYSQL::getPassword);
    }

    @LocalServerPort int port;
    @Autowired JdbcTemplate jdbc;

    @Test
    void flywayCreatesCatalogSchemaWithUninitializedVersion() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM catalog_sync_state WHERE id = 1 AND applied_version IS NULL AND applied_at IS NULL", Integer.class))
            .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1", Integer.class))
            .isEqualTo(1);
    }

    @Autowired ProfessionalCategoryRepository categories;
    @Autowired ProfessionalRepository professionals;
    @Autowired WeeklyScheduleRepository schedules;
    @Autowired CatalogSyncStateRepository syncStates;
    @Autowired EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;

    private static final Instant CREATED = Instant.parse("2026-09-10T13:40:00.123456Z");

    @Test
    @Transactional
    void storesExternalIdsDisabledRecordsAndLocalScheduleTimes() {
        categories.saveAndFlush(new ProfessionalCategoryEntity(101L, "Clínica", null, false, CREATED, CREATED));
        professionals.saveAndFlush(new ProfessionalEntity(201L, 101L, "Ana", "Gómez", false, CREATED, CREATED));
        schedules.saveAndFlush(new WeeklyScheduleEntity(301L, 201L, DayOfWeek.MONDAY,
            LocalTime.of(9, 0), LocalTime.of(12, 0), 30, false, CREATED, CREATED));
        entityManager.clear();
        var category = categories.findById(101L).orElseThrow();
        assertThat(category.getDescription()).isNull();
        assertThat(category.getEnabled()).isFalse();
        assertThat(category.getCreatedAt()).isEqualTo(CREATED);
        var professional = professionals.findById(201L).orElseThrow();
        assertThat(professional.getCategoryId()).isEqualTo(101L);
        assertThat(professional.getLastName()).isEqualTo("Gómez");
        var schedule = schedules.findById(301L).orElseThrow();
        assertThat(schedule.getProfessionalId()).isEqualTo(201L);
        assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.getSlotDurationMinutes()).isEqualTo(30);
        assertThat(schedule.getEnabled()).isFalse();
    }

    @Test
    @Transactional
    void databaseRejectsBrokenReferencesAndInvalidSchedules() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO professional VALUES (999, 888, 'Ana', 'Gomez', TRUE, NOW(), NOW())"))
            .isInstanceOf(DataIntegrityViolationException.class);
        categories.saveAndFlush(new ProfessionalCategoryEntity(102L, "Clínica", "General", true, CREATED, CREATED));
        professionals.saveAndFlush(new ProfessionalEntity(202L, 102L, "Ana", "Gómez", true, CREATED, CREATED));
        String sql = "INSERT INTO weekly_schedule VALUES (302, 202, ?, ?, ?, ?, TRUE, NOW(), NOW())";
        assertThatThrownBy(() -> jdbc.update(sql, "MONDAY", "09:00:00", "12:00:00", 0))
            .isInstanceOf(DataAccessException.class).hasMessageContaining("ck_schedule_duration");
        assertThatThrownBy(() -> jdbc.update(sql, "MONDAY", "12:00:00", "09:00:00", 30))
            .isInstanceOf(DataAccessException.class).hasMessageContaining("ck_schedule_interval");
        assertThatThrownBy(() -> jdbc.update(sql, "INVALID", "09:00:00", "12:00:00", 30))
            .isInstanceOf(DataAccessException.class).hasMessageContaining("ck_schedule_day");
        assertThatThrownBy(() -> jdbc.update("DELETE FROM professional_category WHERE id = 102"))
            .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO catalog_sync_state VALUES (2, NULL, NULL)"))
            .isInstanceOf(DataAccessException.class).hasMessageContaining("ck_sync_single_row");
    }

    @Test
    void transactionRollsBackCatalogDataAndVersionTogether() {
        var transaction = new TransactionTemplate(transactionManager);
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            categories.saveAndFlush(new ProfessionalCategoryEntity(103L, "Rollback", null, true, CREATED, CREATED));
            var state = syncStates.findForUpdate().orElseThrow();
            state.markApplied(7, CREATED);
            syncStates.flush();
            throw new IllegalStateException("simulated interrupted synchronization");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(categories.findById(103L)).isEmpty();
        assertThat(syncStates.findById(1).orElseThrow().getAppliedVersion()).isNull();
    }

    @Test
    @Transactional
    void versionZeroIsValidAndAppliedVersionCannotMoveBackwards() {
        var state = syncStates.findForUpdate().orElseThrow();
        state.markApplied(0, CREATED);
        syncStates.flush();
        entityManager.clear();
        state = syncStates.findForUpdate().orElseThrow();
        assertThat(state.getAppliedVersion()).isZero();
        assertThat(state.getAppliedAt()).isEqualTo(CREATED);
        state.markApplied(7, CREATED);
        final var appliedState = state;
        assertThatThrownBy(() -> appliedState.markApplied(6, CREATED)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> appliedState.markApplied(-1, CREATED)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> appliedState.markApplied(8, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Transactional
    void eveningScheduleKeepsLocalHoursInArgentinaTimezone() {
        var previous = java.util.TimeZone.getDefault();
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Argentina/Mendoza"));
        try {
            categories.saveAndFlush(new ProfessionalCategoryEntity(104L, "Tarde", null, true, CREATED, CREATED));
            professionals.saveAndFlush(new ProfessionalEntity(204L, 104L, "Ana", "Gómez", true, CREATED, CREATED));
            schedules.saveAndFlush(new WeeklyScheduleEntity(304L, 204L, DayOfWeek.MONDAY,
                LocalTime.of(19, 0), LocalTime.of(22, 0), 30, true, CREATED, CREATED));
            assertThat(jdbc.queryForObject("SELECT CAST(start_time AS CHAR) FROM weekly_schedule WHERE id=304", String.class))
                .startsWith("19:00:00");
            assertThat(jdbc.queryForObject("SELECT CAST(end_time AS CHAR) FROM weekly_schedule WHERE id=304", String.class))
                .startsWith("22:00:00");
            entityManager.clear();
            assertThat(schedules.findById(304L).orElseThrow().getEndTime()).isEqualTo(LocalTime.of(22, 0));
        } finally {
            java.util.TimeZone.setDefault(previous);
        }
    }

    @Test
    void categoryEndpointReturnsOnlyEnabledCategoriesWithStableOrderAndPublicFields() throws Exception {
        try {
            categories.saveAllAndFlush(java.util.List.of(
                new ProfessionalCategoryEntity(501L, "Zoología", null, true, CREATED, CREATED),
                new ProfessionalCategoryEntity(502L, "Clínica", "General", true, CREATED, CREATED),
                new ProfessionalCategoryEntity(503L, "Clínica", null, true, CREATED, CREATED),
                new ProfessionalCategoryEntity(504L, "Archivada", null, false, CREATED, CREATED)));
            var response = getCategories();
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type").orElseThrow()).startsWith("application/json");
            var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
            assertThat(mapper.readTree(response.body())).isEqualTo(mapper.readTree("""
                [{"id":502,"name":"Clínica","description":"General"},
                 {"id":503,"name":"Clínica","description":null},
                 {"id":501,"name":"Zoología","description":null}]
                """));
            assertThat(categories.count()).isEqualTo(4);
        } finally {
            categories.deleteAllByIdInBatch(java.util.List.of(501L, 502L, 503L, 504L));
        }
    }

    @Test
    void categoryEndpointReturnsEmptyArrayWithoutEnabledCategories() throws Exception {
        assertThat(getCategories().body()).isEqualTo("[]");
        try {
            categories.saveAndFlush(new ProfessionalCategoryEntity(505L, "Archivada", null, false, CREATED, CREATED));
            var response = getCategories();
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("[]");
        } finally {
            categories.deleteById(505L);
        }
    }

    private HttpResponse<String> getCategories() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/catalog/categories")).build(),
                HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test
    void healthReportsUpWithoutInternalDetails() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/actuator/health")).build(),
                HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("{\"status\":\"UP\"}");
        }
    }
}
