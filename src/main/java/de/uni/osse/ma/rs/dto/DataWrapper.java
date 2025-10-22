package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.DataField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class DataWrapper {
    private final HeadersDto headersDto;
    @Getter
    private final List<HeaderInfo> headers;
    @Getter
    private final List<List<DataField<?>>> rows;

    public DataWrapper(List<String[]> csvValues, HeadersDto headersDto) {
        this.headersDto = headersDto;
        final String[] csvHeaders = csvValues.getFirst();
        var rawValues = csvValues.subList(1, csvValues.size());
        Map<String, Optional<HeaderInfo>> headerMapping = Arrays.stream(csvHeaders)
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
        this.headers = Arrays.stream(csvHeaders).sequential().map(headerMapping::get).map(Optional::get).toList();

        try {
            List<List<DataField<?>>> list = new ArrayList<>();
            for (String[] rawValue : rawValues) {
                List<DataField<?>> parsedRow = new ArrayList<>(rawValue.length);
                try {
                    for (int i = 0; i < rawValue.length; i++) {
                        parsedRow.add(this.headers.get(i).parseValue(rawValue[i]));
                    }
                } catch (IllegalArgumentException e) {
                    log.warn("Could not parse a value of {}", String.join(";", rawValue));
                    continue;
                }

                list.add(parsedRow);
            }
            this.rows = list;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse CSV with the given type information", e);
        }
    }

    public int maxObfuscationFor(HeaderInfo column) {
        return this.headersDto.maxObfuscationFor(column);
    }
}
