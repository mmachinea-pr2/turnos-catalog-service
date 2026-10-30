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
    void professionalsEndpointFiltersCategoryAndLogicalDeletionsWithStableOrder() throws Exception {
        try {
            categories.saveAllAndFlush(java.util.List.of(
                new ProfessionalCategoryEntity(601L, "Clínica", null, true, CREATED, CREATED),
                new ProfessionalCategoryEntity(602L, "Otra", null, true, CREATED, CREATED),
                new ProfessionalCategoryEntity(603L, "Deshabilitada", null, false, CREATED, CREATED)));
            professionals.saveAllAndFlush(java.util.List.of(
                new ProfessionalEntity(701L, 601L, "Zoe", "Zapata", true, CREATED, CREATED),
                new ProfessionalEntity(702L, 601L, "Bruno", "Gómez", true, CREATED, CREATED),
                new ProfessionalEntity(703L, 601L, "Ana", "Gómez", true, CREATED, CREATED),
                new ProfessionalEntity(704L, 601L, "Ana", "Gómez", true, CREATED, CREATED),
                new ProfessionalEntity(705L, 601L, "Oculto", "A", false, CREATED, CREATED),
                new ProfessionalEntity(706L, 602L, "Otra", "Categoría", true, CREATED, CREATED),
                new ProfessionalEntity(707L, 603L, "No", "Visible", true, CREATED, CREATED)));
            var response = getProfessionals("?categoryId=601");
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type").orElseThrow()).startsWith("application/json");
            var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
            assertThat(mapper.readTree(response.body())).isEqualTo(mapper.readTree("""
                [{"id":703,"categoryId":601,"firstName":"Ana","lastName":"Gómez"},
                 {"id":704,"categoryId":601,"firstName":"Ana","lastName":"Gómez"},
                 {"id":702,"categoryId":601,"firstName":"Bruno","lastName":"Gómez"},
                 {"id":701,"categoryId":601,"firstName":"Zoe","lastName":"Zapata"}]
                """));
            assertThat(getProfessionals("?categoryId=603").body()).isEqualTo("[]");
            assertThat(professionals.count()).isEqualTo(7);
        } finally {
            professionals.deleteAllByIdInBatch(java.util.List.of(701L, 702L, 703L, 704L, 705L, 706L, 707L));
            categories.deleteAllByIdInBatch(java.util.List.of(601L, 602L, 603L));
        }
    }

    @Test
    void professionalsEndpointReturnsEmptyForMissingOrEmptyCategory() throws Exception {
        var missing = getProfessionals("?categoryId=699");
        assertThat(missing.statusCode()).isEqualTo(200);
        assertThat(missing.body()).isEqualTo("[]");
        try {
            categories.saveAndFlush(new ProfessionalCategoryEntity(604L, "Vacía", null, true, CREATED, CREATED));
            var response = getProfessionals("?categoryId=604");
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isEqualTo("[]");
        } finally {
            categories.deleteById(604L);
        }
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"", "?categoryId=0", "?categoryId=-1", "?categoryId=abc", "?categoryId=9223372036854775808"})
    void professionalsEndpointRejectsInvalidCategory(String query) throws Exception {
        assertThat(getProfessionals(query).statusCode()).isEqualTo(400);
    }

    private HttpResponse<String> getProfessionals(String query) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/catalog/professionals" + query)).build(),
                HttpResponse.BodyHandlers.ofString());
        }
    }

    static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> professionalNameSearches() {
        return java.util.stream.Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("ana", java.util.List.of(901L)),
            org.junit.jupiter.params.provider.Arguments.of("GÓMEZ", java.util.List.of(901L)),
            org.junit.jupiter.params.provider.Arguments.of("  ANA   gómez  ", java.util.List.of(901L)),
            org.junit.jupiter.params.provider.Arguments.of("ru", java.util.List.of(902L)),
            org.junit.jupiter.params.provider.Arguments.of("", java.util.List.of(901L, 902L)),
            org.junit.jupiter.params.provider.Arguments.of("   ", java.util.List.of(901L, 902L)),
            org.junit.jupiter.params.provider.Arguments.of("inexistente", java.util.List.of()),
            org.junit.jupiter.params.provider.Arguments.of("%", java.util.List.of()),
            org.junit.jupiter.params.provider.Arguments.of("_", java.util.List.of()),
            org.junit.jupiter.params.provider.Arguments.of("' OR 1=1 --", java.util.List.of())
        );
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("professionalNameSearches")
    void professionalNameSearchIsLiteralAndKeepsCategoryAndEnabledFilters(String name, java.util.List<Long> expectedIds) throws Exception {
        try {
            categories.saveAllAndFlush(java.util.List.of(
                new ProfessionalCategoryEntity(801L, "Clínica", null, true, CREATED, CREATED),
                new ProfessionalCategoryEntity(802L, "Otra", null, true, CREATED, CREATED),
                new ProfessionalCategoryEntity(803L, "Deshabilitada", null, false, CREATED, CREATED)));
            professionals.saveAllAndFlush(java.util.List.of(
                new ProfessionalEntity(901L, 801L, "Ana", "Gómez", true, CREATED, CREATED),
                new ProfessionalEntity(902L, 801L, "Bruno", "Ruiz", true, CREATED, CREATED),
                new ProfessionalEntity(903L, 801L, "Ana", "Gómez", false, CREATED, CREATED),
                new ProfessionalEntity(904L, 802L, "Ana", "Gómez", true, CREATED, CREATED),
                new ProfessionalEntity(905L, 803L, "Ana", "Gómez", true, CREATED, CREATED)));
            var query = "?categoryId=801&name=" + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8);
            var response = getProfessionals(query);
            assertThat(response.statusCode()).isEqualTo(200);
            var rows = tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.body());
            var ids = new java.util.ArrayList<Long>();
            for (var row : rows) {
                ids.add(row.path("id").asLong());
                assertThat(row.path("categoryId").asLong()).isEqualTo(801L);
            }
            assertThat(ids).containsExactlyElementsOf(expectedIds);
            assertThat(getProfessionals("?categoryId=803&name=Ana").body()).isEqualTo("[]");
        } finally {
            professionals.deleteAllByIdInBatch(java.util.List.of(901L, 902L, 903L, 904L, 905L));
            categories.deleteAllByIdInBatch(java.util.List.of(801L, 802L, 803L));
        }
    }

    @Test
    void scheduleEndpointReturnsOnlyEnabledAgendaInWeekAndTimeOrder() throws Exception {
        try {
            categories.saveAllAndFlush(java.util.List.of(
                new ProfessionalCategoryEntity(1001L, "Clínica", null, true, CREATED, CREATED),
                new ProfessionalCategoryEntity(1002L, "Deshabilitada", null, false, CREATED, CREATED)));
            professionals.saveAllAndFlush(java.util.List.of(
                new ProfessionalEntity(1101L, 1001L, "Ana", "Gómez", true, CREATED, CREATED),
                new ProfessionalEntity(1102L, 1001L, "No", "Visible", false, CREATED, CREATED),
                new ProfessionalEntity(1103L, 1002L, "Otra", "Categoría", true, CREATED, CREATED),
                new ProfessionalEntity(1104L, 1001L, "Sin", "Horarios", true, CREATED, CREATED)));
            schedules.saveAllAndFlush(java.util.List.of(
                schedule(1201L, 1101L, DayOfWeek.FRIDAY, 19, 22, true),
                schedule(1202L, 1101L, DayOfWeek.MONDAY, 14, 17, true),
                schedule(1203L, 1101L, DayOfWeek.SUNDAY, 9, 12, true),
                schedule(1208L, 1101L, DayOfWeek.MONDAY, 9, 12, true),
                schedule(1204L, 1101L, DayOfWeek.MONDAY, 9, 12, true),
                schedule(1209L, 1101L, DayOfWeek.MONDAY, 9, 11, true),
                schedule(1205L, 1101L, DayOfWeek.MONDAY, 7, 8, false),
                schedule(1206L, 1102L, DayOfWeek.MONDAY, 9, 12, true),
                schedule(1207L, 1103L, DayOfWeek.MONDAY, 9, 12, true)));
            var response = getSchedules("1101");
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type").orElseThrow()).startsWith("application/json");
            var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
            assertThat(mapper.readTree(response.body())).isEqualTo(mapper.readTree("""
                [{"id":1209,"professionalId":1101,"dayOfWeek":"MONDAY","startTime":"09:00:00","endTime":"11:00:00","slotDurationMinutes":30},
                 {"id":1204,"professionalId":1101,"dayOfWeek":"MONDAY","startTime":"09:00:00","endTime":"12:00:00","slotDurationMinutes":30},
                 {"id":1208,"professionalId":1101,"dayOfWeek":"MONDAY","startTime":"09:00:00","endTime":"12:00:00","slotDurationMinutes":30},
                 {"id":1202,"professionalId":1101,"dayOfWeek":"MONDAY","startTime":"14:00:00","endTime":"17:00:00","slotDurationMinutes":30},
                 {"id":1201,"professionalId":1101,"dayOfWeek":"FRIDAY","startTime":"19:00:00","endTime":"22:00:00","slotDurationMinutes":30},
                 {"id":1203,"professionalId":1101,"dayOfWeek":"SUNDAY","startTime":"09:00:00","endTime":"12:00:00","slotDurationMinutes":30}]
                """));
            for (var id : java.util.List.of("1102", "1103", "1104", "1199")) {
                var empty = getSchedules(id);
                assertThat(empty.statusCode()).isEqualTo(200);
                assertThat(empty.body()).isEqualTo("[]");
            }
            assertThat(schedules.count()).isEqualTo(9);
        } finally {
            schedules.deleteAllByIdInBatch(java.util.List.of(1201L, 1202L, 1203L, 1204L, 1205L, 1206L, 1207L, 1208L, 1209L));
            professionals.deleteAllByIdInBatch(java.util.List.of(1101L, 1102L, 1103L, 1104L));
            categories.deleteAllByIdInBatch(java.util.List.of(1001L, 1002L));
        }
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"0", "-1", "abc", "9223372036854775808"})
    void scheduleEndpointRejectsInvalidProfessional(String id) throws Exception {
        assertThat(getSchedules(id).statusCode()).isEqualTo(400);
    }

    private static WeeklyScheduleEntity schedule(long id, long professionalId, DayOfWeek day, int start, int end, boolean enabled) {
        return new WeeklyScheduleEntity(id, professionalId, day, LocalTime.of(start, 0), LocalTime.of(end, 0),
            30, enabled, CREATED, CREATED);
    }

    private HttpResponse<String> getSchedules(String id) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/catalog/professionals/" + id + "/schedules")).build(),
                HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test
    void professionalsPaginationPreservesFiltersCountsAndStableOrder() throws Exception {
        var ids = new java.util.ArrayList<Long>();
        var entities = new java.util.ArrayList<ProfessionalEntity>();
        try {
            categories.saveAllAndFlush(java.util.List.of(
                new ProfessionalCategoryEntity(2001L, "Paginada", null, true, CREATED, CREATED),
                new ProfessionalCategoryEntity(2002L, "Otra", null, true, CREATED, CREATED)));
            for (int i = 0; i < 24; i++) {
                long id = 2101L + i;
                ids.add(id);
                entities.add(new ProfessionalEntity(id, 2001L, "Nombre", "Apellido", true, CREATED, CREATED));
            }
            ids.addAll(java.util.List.of(2201L, 2202L, 2203L));
            entities.add(new ProfessionalEntity(2201L, 2001L, "Bruno", "Otro", true, CREATED, CREATED));
            entities.add(new ProfessionalEntity(2202L, 2001L, "Nombre", "Apellido", false, CREATED, CREATED));
            entities.add(new ProfessionalEntity(2203L, 2002L, "Nombre", "Apellido", true, CREATED, CREATED));
            professionals.saveAllAndFlush(entities);
            var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
            var defaults = getProfessionals("?categoryId=2001");
            assertThat(defaults.statusCode()).isEqualTo(200);
            assertThat(mapper.readTree(defaults.body()).size()).isEqualTo(20);
            assertThat(defaults.headers().firstValue("X-Total-Count")).contains("25");
            var actualIds = new java.util.ArrayList<Long>();
            for (int page = 0; page < 4; page++) {
                var response = getProfessionals("?categoryId=2001&name=nombre&size=10&page=" + page);
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headers().firstValue("X-Total-Count")).contains("24");
                var rows = mapper.readTree(response.body());
                assertThat(rows.size()).isEqualTo(page < 2 ? 10 : page == 2 ? 4 : 0);
                for (var row : rows) actualIds.add(row.path("id").asLong());
            }
            assertThat(actualIds).containsExactlyElementsOf(ids.subList(0, 24));
            var noMatches = getProfessionals("?categoryId=2001&name=inexistente");
            assertThat(noMatches.body()).isEqualTo("[]");
            assertThat(noMatches.headers().firstValue("X-Total-Count")).contains("0");
            var maxSize = getProfessionals("?categoryId=2001&size=100");
            assertThat(maxSize.statusCode()).isEqualTo(200);
            assertThat(mapper.readTree(maxSize.body()).size()).isEqualTo(25);
        } finally {
            professionals.deleteAllByIdInBatch(ids);
            categories.deleteAllByIdInBatch(java.util.List.of(2001L, 2002L));
        }
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {
        "page=-1", "size=0", "size=-1", "size=101", "page=abc", "size=abc",
        "page=2147483648", "size=2147483648", "page=2147483647&size=2"})
    void professionalsEndpointRejectsInvalidPagination(String query) throws Exception {
        assertThat(getProfessionals("?categoryId=1&" + query).statusCode()).isEqualTo(400);
    }

    @Test
    void synchronizationStatusReportsNotInitializedWithoutTriggeringLoad() throws Exception {
        var response = getSynchronizationStatus();
        assertThat(response.statusCode()).isEqualTo(200);
        var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
        assertThat(mapper.readTree(response.body())).isEqualTo(mapper.readTree("""
            {"initialized":false,"appliedVersion":null,"appliedAt":null}
            """));
        assertThat(categories.count()).isZero();
        assertThat(syncStates.findById(1).orElseThrow().getAppliedVersion()).isNull();
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(longs = {0, 7})
    void synchronizationStatusReportsCommittedVersionAndUtcTime(long version) throws Exception {
        try {
            var transaction = new TransactionTemplate(transactionManager);
            transaction.executeWithoutResult(status -> {
                var state = syncStates.findForUpdate().orElseThrow();
                state.markApplied(version, CREATED);
                syncStates.flush();
            });
            var response = getSynchronizationStatus();
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type").orElseThrow()).startsWith("application/json");
            var body = tools.jackson.databind.json.JsonMapper.builder().build().readTree(response.body());
            assertThat(body.size()).isEqualTo(3);
            assertThat(body.path("initialized").asBoolean()).isTrue();
            assertThat(body.path("appliedVersion").asLong()).isEqualTo(version);
            assertThat(Instant.parse(body.path("appliedAt").asText())).isEqualTo(CREATED);
            assertThat(body.path("appliedAt").asText()).endsWith("Z");
            assertThat(syncStates.findById(1).orElseThrow().getAppliedAt()).isEqualTo(CREATED);
        } finally {
            jdbc.update("UPDATE catalog_sync_state SET applied_version=NULL, applied_at=NULL WHERE id=1");
        }
    }

    @Test
    void missingSynchronizationStateIsAnErrorRatherThanUninitialized() throws Exception {
        jdbc.update("DELETE FROM catalog_sync_state WHERE id=1");
        try {
            assertThat(getSynchronizationStatus().statusCode()).isEqualTo(500);
        } finally {
            jdbc.update("INSERT INTO catalog_sync_state(id, applied_version, applied_at) VALUES (1, NULL, NULL)");
        }
    }

    private HttpResponse<String> getSynchronizationStatus() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/catalog/synchronization/status")).build(),
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
