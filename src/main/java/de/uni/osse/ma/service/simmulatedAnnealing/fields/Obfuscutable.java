package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public interface Obfuscutable {

    String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException;

}
