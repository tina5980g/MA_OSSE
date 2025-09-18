package de.uni.osse.ma.exceptions;

import jakarta.annotation.Nonnull;

public class NoMoreAnonymizationLevelsException extends RuntimeException {

    public <T extends Enum<T>> NoMoreAnonymizationLevelsException(@Nonnull T enumValue) {
        super("There are no more anonymization levels for " + enumValue.name() + " of " + enumValue.getClass().getSimpleName());
    }

    public NoMoreAnonymizationLevelsException(int requestedAnonymizationLevel) {
        super("There is no anonymization level of " + requestedAnonymizationLevel);
    }
}
