package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscutable;

public enum Sex implements Obfuscutable {
    MALE,
    FEMALE
    ;


    @Override
    public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
        if (level == 0) return this.name();
        if (level == 1) return "*";
        throw new NoMoreAnonymizationLevelsException(level);
    }
}
