package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.DataField;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class DataWrapper {
    private final List<HeaderInfo> headers;
    // TODO: can this also be Stream based?
    private final List<List<DataField<?>>> rows;

    public DataWrapper(List<String[]> csvValues, HeadersDto headersDto) {
        final String[] headers = csvValues.getFirst();
        var rawValues = csvValues.subList(1, csvValues.size());
        Map<String, Optional<HeaderInfo>> headerMapping = Arrays.stream(headers)
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                headerId -> headersDto.columns().stream().filter(header -> header.columnIdentifier().equalsIgnoreCase(headerId)).findAny()));


        List<String> unknownHeaders = headerMapping.entrySet().stream().filter(entry -> entry.getValue().isEmpty()).map(Map.Entry::getKey).toList();
        if (!unknownHeaders.isEmpty()) {
            throw new IllegalArgumentException("Not all columns of the CSV are mapped into known headerInfo: " + String.join(", ", unknownHeaders));
        }
        // we checked previously that no empty optionals exist in the complete mapping
        this.headers = Arrays.stream(headers).sequential().map(headerMapping::get).map(Optional::get).toList();

        try {
            List<List<DataField<?>>> list = new ArrayList<>();
            for (String[] rawValue : rawValues) {
                List<DataField<?>> parsedRow = new ArrayList<>(rawValue.length);
                for (int i = 0; i < rawValue.length; i++) {
                    parsedRow.add(this.headers.get(i).parseValue(rawValue[i]));
                }

                list.add(parsedRow);
            }
            this.rows = list;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse CSV with the given type information", e);
        }
    }
}
