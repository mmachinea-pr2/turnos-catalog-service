package ar.edu.programacion2.catalog.catalog.infrastructure.rest;

import ar.edu.programacion2.catalog.catalog.application.port.out.SnapshotFetchException;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class RestCatalogSnapshotSourceTests {
    private MockRestServiceServer server;
    private RestCatalogSnapshotSource source;
    private String fixture;

    @BeforeEach
    void setup() throws Exception {
        var builder = RestClient.builder().baseUrl("http://catedra.test")
            .defaultHeaders(headers -> headers.setBearerAuth("test-token"));
        server = MockRestServiceServer.bindTo(builder).build();
        source = new RestCatalogSnapshotSource(builder.build());
        fixture = new ClassPathResource("catalog/snapshot.json").getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void requestsAuthenticatedSnapshotAndMapsBothTimeFormatsAndDisabledRecords() {
        server.expect(requestTo("http://catedra.test/api/synchronization/snapshot"))
            .andExpect(method(HttpMethod.GET)).andExpect(header("Authorization", "Bearer test-token"))
            .andExpect(header("Accept", "application/json"))
            .andRespond(withSuccess(fixture, MediaType.APPLICATION_JSON));
        var snapshot = source.fetch();
        assertThat(snapshot.snapshotVersion()).isEqualTo(7);
        assertThat(snapshot.professionalCategories().getFirst().enabled()).isFalse();
        assertThat(snapshot.professionals().getFirst().categoryId()).isEqualTo(1);
        var schedule = snapshot.weeklySchedules().getFirst();
        assertThat(schedule.startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.endTime()).isEqualTo(LocalTime.NOON);
        server.verify();
    }

    @Test
    void acceptsEmptySnapshotAtVersionZero() {
        respond("{\"snapshotVersion\":0,\"generatedAt\":\"2026-09-10T13:40:00Z\","
            + "\"professionalCategories\":[],\"professionals\":[],\"weeklySchedules\":[]}");
        assertThat(source.fetch().snapshotVersion()).isZero();
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {301, 401, 403, 500, 503})
    void rejectsUnsuccessfulStatusWithoutExposingResponseBody(int status) {
        server.expect(requestTo("http://catedra.test/api/synchronization/snapshot"))
            .andRespond(withStatus(HttpStatusCode.valueOf(status)).body("private-response"));
        assertThatThrownBy(source::fetch).isInstanceOf(SnapshotFetchException.class)
            .hasMessage("Snapshot request failed: HTTP " + status).hasNoCause();
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"snapshotVersion", "generatedAt", "professionalCategories", "professionals", "weeklySchedules", "enabled", "slotDurationMinutes"})
    void rejectsMissingRequiredFieldsInsteadOfAssumingDefaults(String field) {
        // Cambiar el nombre equivale a omitir el campo y agregar uno desconocido.
        respond(fixture.replace("\"" + field + "\"", "\"unknownField\""));
        assertInvalid();
    }

    @Test
    void rejectsBrokenReferences() {
        respond(fixture.replace("\"categoryId\": 1", "\"categoryId\": 999"));
        assertInvalid();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{}", "not-json", "{\"snapshotVersion\":null}"})
    void rejectsEmptyOrMalformedBodies(String body) {
        respond(body);
        assertInvalid();
    }

    @Test
    void translatesNetworkFailureWithoutLeakingUnderlyingMessage() {
        server.expect(requestTo("http://catedra.test/api/synchronization/snapshot"))
            .andRespond(withException(new SocketTimeoutException("private-network-detail")));
        assertThatThrownBy(source::fetch).isInstanceOf(SnapshotFetchException.class)
            .hasMessage("Snapshot server unavailable or request timed out").hasNoCause();
        server.verify();
    }

    private void respond(String body) {
        server.expect(requestTo("http://catedra.test/api/synchronization/snapshot"))
            .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    private void assertInvalid() {
        assertThatThrownBy(source::fetch).isInstanceOf(SnapshotFetchException.class)
            .hasMessage("Invalid snapshot response").hasNoCause();
        server.verify();
    }
}
