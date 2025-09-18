package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

public interface ObfuscationLevelRepresentation {

    ObfuscationLevelRepresentation getNextLevel();
    @Override
    String toString();
}
