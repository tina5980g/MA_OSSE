package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public enum StaticRepresentations implements ObfuscationLevelRepresentation{
    LAST_LEVEL,
    ;

    @Override
    public ObfuscationLevelRepresentation getNextLevel() {
        throw new NoMoreAnonymizationLevelsException(this);
    }


    @Override
    public String toString() {
        return switch (this) {
            case LAST_LEVEL -> "*";
        };
    }
}