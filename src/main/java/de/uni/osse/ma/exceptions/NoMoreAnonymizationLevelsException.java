package de.uni.osse.ma.exceptions;

public class NoMoreAnonymizationLevelsException extends RuntimeException {

    public NoMoreAnonymizationLevelsException(int requestedAnonymizationLevel) {
        super("There is no anonymization level of " + requestedAnonymizationLevel);
    }
}
