package com.bartoszmatras.cdq.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserServiceTest {

    private final CsvParserService csvParserService = new CsvParserService();

    @Test
    void shouldParseCsvAndReturnChunks() throws IOException {
        // given
        var csvData = """
                iban,date,currency,category,amount
                IBAN1,2023-01-01,EUR,FOOD,10.0
                IBAN2,2023-01-02,USD,ENTERTAINMENT,20.0
                IBAN3,2023-01-03,GBP,TRANSPORT,30.0
            """;

        // when & then
        try (var parser = csvParserService.parse(new StringReader(csvData))) {
            var chunk1 = parser.nextChunk(2);
            assertEquals(2, chunk1.size());
            assertEquals("IBAN1", chunk1.getFirst().getIban());
            assertEquals("2023-01-01", chunk1.getFirst().getDate());
            assertEquals("EUR", chunk1.getFirst().getCurrency());
            assertEquals("FOOD", chunk1.getFirst().getCategory());
            assertEquals("10.0", chunk1.getFirst().getAmount());
            assertEquals(2, chunk1.getFirst().getLineNumber());

            assertEquals("IBAN2", chunk1.get(1).getIban());
            assertEquals(3, chunk1.get(1).getLineNumber());

            var chunk2 = parser.nextChunk(2);
            assertEquals(1, chunk2.size());
            assertEquals("IBAN3", chunk2.getFirst().getIban());
            assertEquals(4, chunk2.getFirst().getLineNumber());

            var chunk3 = parser.nextChunk(2);
            assertTrue(chunk3.isEmpty());
        }
    }

    @Test
    void shouldHandleMissingColumnsSafely() throws IOException {
        // given
        var csvData = "iban,date\n" +
                "IBAN1,,EUR,";

        // when & then
        try (var parser = csvParserService.parse(new StringReader(csvData))) {
            var chunk = parser.nextChunk(1);
            assertEquals(1, chunk.size());
            assertEquals("IBAN1", chunk.getFirst().getIban());
            assertEquals("", chunk.getFirst().getDate());
            assertEquals("EUR", chunk.getFirst().getCurrency());
            assertEquals("", chunk.getFirst().getCategory());
            assertNull(chunk.getFirst().getAmount());
        }
    }

    @Test
    void shouldIgnoreEmptyLines() throws IOException {
        // given
        var csvData = """
                iban,date,currency,category,amount
                \n
                IBAN1,2023-01-01,EUR,FOOD,10.0
                \n
                """;

        // when & then
        try (var parser = csvParserService.parse(new StringReader(csvData))) {
            var chunk = parser.nextChunk(10);
            assertEquals(1, chunk.size());
            assertEquals("IBAN1", chunk.getFirst().getIban());
            assertEquals(2, chunk.getFirst().getLineNumber());
        }
    }
}
