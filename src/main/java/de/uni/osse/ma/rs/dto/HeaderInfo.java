package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.DataField;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

@Builder
// TODO: sufficient serialization?
public record HeaderInfo(@Nonnull String columnName, @Nonnull String columnIdentifier, @Nullable DataType dataType, @Nonnull ColumnType columnType) implements Serializable {


    public HeaderInfo {
        if (columnType == ColumnType.PSEUDO_IDENTIFIER && dataType == null) {
            throw new IllegalArgumentException("A dataType is required for pseudo identifiers");
        }
    }

    public DataField<?> parseValue(String rawValue) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return dataType().getRepresentingClass().getDeclaredConstructor(String.class).newInstance(rawValue);
    }

    @Override
    @Nonnull
    public DataType dataType() {
        return switch (columnType) {
            case UNDEFINED -> DataType.NONIDENTIFIER;
            case IDENTIFIER -> DataType.IDENTIFIER;
            case PSEUDO_IDENTIFIER -> {
                if (dataType == null) {
                    throw new IllegalArgumentException("A dataType is required for pseudo identifiers");
                }
                yield dataType;
            }
            case CLASSIFICATION_TARGET -> {
                if (dataType == null) yield  DataType.NONIDENTIFIER;
                yield dataType;
            }
        };
    }
}
