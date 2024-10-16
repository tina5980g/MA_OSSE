package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public class NonIdentifyingField extends DataField<String>{

    public NonIdentifyingField(String rawValue) {
        super(rawValue);
    }

    @Override
    protected String parse(String rawValue) {
        return rawValue;
    }

    @Override
    public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
        return this.internalValue;
    }
}
