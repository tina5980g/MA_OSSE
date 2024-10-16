package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public class IdentifyingField extends DataField<String> {

    public IdentifyingField(String rawValue) {
        super(rawValue);
    }

    @Override
    protected String parse(String rawValue) {
        return rawValue;
    }

    @Override
    public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
        return "*";
    }
}
