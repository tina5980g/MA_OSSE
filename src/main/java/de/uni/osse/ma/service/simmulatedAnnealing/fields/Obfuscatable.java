package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public interface Obfuscatable {

    String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException;

}
