package ar.edu.programacion2.catalog.catalog.infrastructure.messaging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import static org.assertj.core.api.Assertions.*;

class CatalogUpdatedEventDecoderTests {
    private static final String JSON = """
        {"eventId":"event-7","eventType":"CatalogUpdated","newVersion":7,
        "occurredAt":"2026-09-10T13:44:59Z","source":"MANUAL","schemaVersion":1,"extra":true}
        """;
    private final CatalogUpdatedEventDecoder decoder = new CatalogUpdatedEventDecoder();

    @Test
    void acceptsContractAndIgnoresExtraFields() {
        var event = decoder.decode("7", JSON);
        assertThat(event.eventId()).isEqualTo("event-7");
        assertThat(event.newVersion()).isEqualTo(7);
        assertThat(event.occurredAt()).hasToString("2026-09-10T13:44:59Z");
        assertThat(event.source()).isEqualTo("MANUAL");
        assertThat(decoder.decode("0", JSON.replace("\"newVersion\":7", "\"newVersion\":0")).newVersion()).isZero();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"8", "07", " 7", "7.0"})
    void rejectsMismatchedKeys(String key) {
        assertThatThrownBy(() -> decoder.decode(key, JSON))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid CatalogUpdated event").hasNoCause();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"{}", "[]", "null", "not-json"})
    void rejectsMalformedMessages(String json) {
        assertThatIllegalArgumentException().isThrownBy(() -> decoder.decode("7", json));
    }

    @ParameterizedTest
    @CsvSource({"schemaVersion,2", "schemaVersion,\"1\"", "schemaVersion,1.5", "eventType,\"OtherEvent\"",
        "eventId,\" \"", "eventId,null", "newVersion,-1", "newVersion,7.5", "newVersion,\"7\"",
        "newVersion,9223372036854775808", "occurredAt,\"invalid-date\"", "source,\"\"", "source,null"})
    void validatesRequiredFields(String field, String value) {
        var mapper = JsonMapper.builder().build();
        var json = (ObjectNode) mapper.readTree(JSON);
        json.set(field, mapper.readTree(value));
        assertThatIllegalArgumentException().isThrownBy(() -> decoder.decode("7", json.toString()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"eventId", "eventType", "schemaVersion", "newVersion", "occurredAt", "source"})
    void rejectsMissingFields(String field) {
        var json = (ObjectNode) JsonMapper.builder().build().readTree(JSON);
        json.remove(field);
        assertThatIllegalArgumentException().isThrownBy(() -> decoder.decode("7", json.toString()));
    }

    @Test
    void rejectsOversizedIdsAndTrailingDocuments() {
        assertThatIllegalArgumentException().isThrownBy(() -> decoder.decode("7", JSON.replace("event-7", "x".repeat(256))));
        assertThatIllegalArgumentException().isThrownBy(() -> decoder.decode("7", JSON + " {}"));
    }
}
