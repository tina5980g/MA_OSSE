package de.uni.osse.ma.rs.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
public record HeadersDto(List<HeaderInfo> columns) {

    public HeadersDto {
        // ensure that columns are uniquely referenced
        var duplicates = columns.stream()
                .map(HeaderInfo::columnIdentifier)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .toList();
        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("Found a duplicate description for columns " + String.join(", ", duplicates));
        }

        // ensure that we have at least one pseudo identifier and at least one target
        var columnTypes = columns.stream().map(HeaderInfo::columnType).distinct().toList();
        if (!columnTypes.contains(ColumnType.PSEUDO_IDENTIFIER)) {
            throw new IllegalArgumentException("no pseudo-identifier column found");
        }
        if (!columnTypes.contains(ColumnType.CLASSIFICATION_TARGET)) {
            throw new IllegalArgumentException("no classification target column found");
        }
    }
}
