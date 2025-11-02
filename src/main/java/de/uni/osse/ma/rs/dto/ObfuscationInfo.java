package de.uni.osse.ma.rs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public record ObfuscationInfo(@Nonnull @Schema(description = "Determines which fields are required") ObfuscationStrategy strategy,
                              @Nullable @Schema(description = "Required for STATIC strategy") DataType dataType,
                              @Nullable @Schema(description = "Required for PROVIDED strategy") Integer level,
                              @Nullable @Schema(description = "Additional params for STATIC strategy. Recognized values depend on dataType") Map<String, Object> params) implements Serializable {

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
