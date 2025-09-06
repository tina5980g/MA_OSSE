package de.uni.osse.ma.rs.dto;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.Objects;

public record ObfuscationInfo(@Nonnull ObfuscationStrategy strategy, @Nullable DataType dataType, @Nullable Integer level) implements Serializable {

    public ObfuscationInfo {
        Objects.requireNonNull(strategy);
        switch (strategy) {
            case STATIC -> Objects.requireNonNull(dataType);
            case PROVIDED -> Objects.requireNonNull(level);
        }
    }

    public enum ObfuscationStrategy {
        STATIC,
        PROVIDED
    }
}
