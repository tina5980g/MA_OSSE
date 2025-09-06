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
    }

    public int maxObfuscationFor(HeaderInfo column) {
        return maxObfuscationFor(column.columnName());
    }

    public int maxObfuscationFor(String columnName) {
        List<HeaderInfo> list = columns.stream().filter(headerInfo -> headerInfo.columnName().equals(columnName)).toList();
        if (list.isEmpty()) {
            throw new IllegalArgumentException("No column with name " + columnName + " found");
        }
        HeaderInfo headerInfo = list.getFirst();
        return switch (headerInfo.obfuscationInfo().strategy()) {
            case STATIC -> {
                if (list.size() == 1) {
                    yield headerInfo.dataType().getMaxObfuscation();
                }
                throw new IllegalStateException("Found more than one column with name " + columnName + " on STATIC strategy");
            }
            case PROVIDED -> list.size() - 1;
        };

    }
}
