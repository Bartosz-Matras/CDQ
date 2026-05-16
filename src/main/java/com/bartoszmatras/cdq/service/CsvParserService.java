package com.bartoszmatras.cdq.service;

import com.bartoszmatras.cdq.dto.CsvTransactionRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class CsvParserService {

    private static final String[] HEADERS = {"iban", "date", "currency", "category", "amount"};

    public CsvParser parse(Reader reader) throws IOException {
        var format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get();
        var csvParser = CSVParser.builder()
                .setReader(reader)
                .setFormat(format)
                .get();
        return new CsvParser(csvParser);
    }

    public static class CsvParser implements AutoCloseable {
        private final CSVParser parser;
        private final Iterator<CSVRecord> iterator;
        private int currentLine = 1;

        CsvParser(CSVParser parser) {
            this.parser = parser;
            this.iterator = parser.iterator();
        }

        public List<CsvTransactionRow> nextChunk(int chunkSize) {
            List<CsvTransactionRow> chunk = new ArrayList<>(chunkSize);
            while (iterator.hasNext() && chunk.size() < chunkSize) {
                currentLine++;
                var csvRecord = iterator.next();
                chunk.add(CsvTransactionRow.builder()
                        .lineNumber(currentLine)
                        .iban(getValueSafe(csvRecord, "iban"))
                        .date(getValueSafe(csvRecord, "date"))
                        .currency(getValueSafe(csvRecord, "currency"))
                        .category(getValueSafe(csvRecord, "category"))
                        .amount(getValueSafe(csvRecord, "amount"))
                        .build());
            }
            return chunk;
        }

        private String getValueSafe(CSVRecord csvRecord, String header) {
            try {
                return csvRecord.get(header);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public void close() throws IOException {
            parser.close();
        }
    }
}