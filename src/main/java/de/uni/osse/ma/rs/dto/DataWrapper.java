package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.DataField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
public class DataWrapper {
    private final HeadersDto headersDto;
    @Getter
    private final List<HeaderInfo> headers;
    @Getter
    private final Stream<List<DataField<?>>> rows;

    public DataWrapper(Spliterator<String[]> csvValues, HeadersDto headersDto) {
        this.headersDto = headersDto;
        AtomicReference<String[]> csvHeaders = new AtomicReference<>();
        if (!csvValues.tryAdvance(csvHeaders::set)) throw new IllegalArgumentException("CSV header not found.");

        Map<String, Optional<HeaderInfo>> headerMapping = Arrays.stream(csvHeaders.get())
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                headerId -> headersDto.columns().stream()
                                        .filter(header -> header.columnIdentifier().equalsIgnoreCase(headerId))
                                        .findAny()
                                ));



        List<String> unknownHeaders = headerMapping.entrySet().stream().filter(entry -> entry.getValue().isEmpty()).map(Map.Entry::getKey).toList();
        if (!unknownHeaders.isEmpty()) {
            throw new IllegalArgumentException("Not all columns of the CSV are mapped into known headerInfo: " + String.join(", ", unknownHeaders));
        }
        // we checked previously that no empty optionals exist in the complete mapping
        this.headers = Arrays.stream(csvHeaders.get()).sequential().map(headerMapping::get).map(Optional::get).toList();

        // the remaining rows are processed as a stream
        try {
            this.rows = StreamSupport.stream(csvValues, false).map(rawRow -> {
                List<DataField<?>> parsedRow = new ArrayList<>(rawRow.length);
                try {
                    for (int i = 0; i < rawRow.length; i++) {
                        parsedRow.add(this.headers.get(i).parseValue(rawRow[i]));
                    }
                } catch (Exception e) {
                    log.warn("Could not parse a value of {}", String.join(";", rawRow));
                    throw new RuntimeException(e);
                }

                return parsedRow;
            });
        } catch (Exception e) {
            log.warn("Failed to parse", e);
            throw new IllegalArgumentException("Failed to parse CSV with the given type information", e);
        }
    }

    public int maxObfuscationFor(HeaderInfo column) {
        return this.headersDto.maxObfuscationFor(column);
    }
}
