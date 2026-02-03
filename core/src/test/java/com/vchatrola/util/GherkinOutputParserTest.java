package com.vchatrola.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GherkinOutputParserTest {

    @Test
    void parseOutput_acceptsWrappedJsonArray() throws Exception {
        String input = "prefix [{\"title\":\"Scenario\",\"status\":\"Valid\",\"reason\":\"NA\",\"suggestion\":\"Valid\"}] suffix";
        String output = GherkinOutputParser.parseOutput(input);
        assertTrue(output.contains("Title|Scenario"));
        assertTrue(output.contains("Status|Valid"));
    }

    @Test
    void parseOutput_marksMissingFieldsInvalid() throws Exception {
        String input = "[{\"title\":\"Scenario\",\"status\":\"Valid\",\"reason\":\"NA\"}]";
        String output = GherkinOutputParser.parseOutput(input);
        assertTrue(output.contains("Status|Invalid"));
        assertTrue(output.contains("Missing required field(s): suggestion"));
    }

    @Test
    void parseOutput_rejectsNonArrayJson() {
        String input = "{\"title\":\"Scenario\"}";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GherkinOutputParser.parseOutput(input));
        assertTrue(ex.getMessage().contains("did not contain a JSON array"));
    }

    @Test
    void parseOutput_rejectsMissingJsonArray() {
        String input = "no json here";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> GherkinOutputParser.parseOutput(input));
        assertTrue(ex.getMessage().contains("did not contain a JSON array"));
    }

    @Test
    void parseOutput_handlesNullFields() throws Exception {
        String input = "[{\"title\":null,\"status\":\"Valid\",\"reason\":\"NA\",\"suggestion\":null}]";
        String output = GherkinOutputParser.parseOutput(input);
        assertTrue(output.contains("Status|Invalid"));
        assertTrue(output.contains("Missing required field(s): title, suggestion"));
    }

    @Test
    void parseOutput_acceptsUnknownFields() throws Exception {
        String input = "[{\"title\":\"Scenario\",\"status\":\"Valid\",\"reason\":\"NA\",\"suggestion\":\"OK\",\"extra\":123}]";
        String output = GherkinOutputParser.parseOutput(input);
        assertTrue(output.contains("Title|Scenario"));
        assertTrue(output.contains("Status|Valid"));
    }
}
