package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.DataField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.IdentifyingField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.NonIdentifyingField;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.lang.reflect.InvocationTargetException;

@Builder
public record HeaderInfo(@Nonnull String columnName, @Nonnull String columnIdentifier, @Nullable DataType dataType, @Nonnull ColumnType columnType) {

    public HeaderInfo {
        if (columnType == ColumnType.PSEUDO_IDENTIFIER && dataType == null) {
            throw new IllegalArgumentException("A dataType is required for pseudo identifiers");
        }
    }

    public DataField<?> parseValue(String rawValue) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var result = switch (columnType) {
            case UNDEFINED, CLASSIFICATION_TARGET -> new NonIdentifyingField(rawValue);
            case IDENTIFIER -> new IdentifyingField(rawValue);
            case PSEUDO_IDENTIFIER -> null;
        };

        if (result == null) {
            return dataType.getRepresentingClass().getDeclaredConstructor(String.class).newInstance(rawValue);
        }
        return result;
    }
}
