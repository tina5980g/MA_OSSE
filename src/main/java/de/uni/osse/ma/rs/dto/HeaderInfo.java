package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.DataField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.RawField;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

@Builder
public record HeaderInfo(@Nonnull String columnName, @Nonnull String columnIdentifier, @Nonnull ObfuscationInfo obfuscationInfo) implements Serializable {

    public HeaderInfo(@Nonnull String columnName, @Nonnull String columnIdentifier, @Nullable ObfuscationInfo obfuscationInfo) {
        Objects.requireNonNull(columnName);
        Objects.requireNonNull(columnIdentifier);

        this.columnName = columnName;
        this.columnIdentifier = columnIdentifier;
        this.obfuscationInfo = Objects.requireNonNullElseGet(obfuscationInfo, () -> new ObfuscationInfo(ObfuscationInfo.ObfuscationStrategy.STATIC, DataType.NONIDENTIFIER, null));
    }

    public DataField<?> parseValue(String rawValue) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (rawValue == null || rawValue.equalsIgnoreCase("nan")) {
            throw new IllegalArgumentException("rawValue cannot be null or empty");
        }

        return switch (obfuscationInfo.strategy()) {
            case STATIC -> dataType().getRepresentingClass().getDeclaredConstructor(String.class).newInstance(rawValue);
            case PROVIDED -> new RawField(rawValue);
        };
    }

    @Nonnull
    public DataType dataType() {
        if (obfuscationInfo.strategy() != ObfuscationInfo.ObfuscationStrategy.STATIC) {
            throw new IllegalStateException("dataType is only supported for STATIC strategy");
        }
        return Objects.requireNonNull(obfuscationInfo().dataType());
    }
}
