package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

import java.util.Map;

public class IdentifyingField extends StringField {

    public IdentifyingField(String rawValue) {
        super(rawValue);
    }

    @Override
    public String representWithObfuscation(int level, Map<String, Object> params) throws NoMoreAnonymizationLevelsException {
        return "*";
    }
}
