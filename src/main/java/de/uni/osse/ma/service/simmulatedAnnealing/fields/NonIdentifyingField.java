package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public class NonIdentifyingField extends StringField {

    public NonIdentifyingField(String rawValue) {
        super(rawValue);
    }

    @Override
    public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
        return this.internalValue;
    }
}
