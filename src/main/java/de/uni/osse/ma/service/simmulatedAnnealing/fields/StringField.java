package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

import java.util.Map;

public class StringField extends DataField<String> {

    public StringField(String rawValue) {
        super(rawValue);
    }

    @Override
    protected String parse(String rawValue) {
        return rawValue;
    }

    @Override
    public String representWithObfuscation(int level, Map<String, Object> params) throws NoMoreAnonymizationLevelsException {
        return switch (level) {
            case 0 -> this.internalValue;
            // TODO: handle mapping param
            case 1 -> "*";
            default -> throw new NoMoreAnonymizationLevelsException(level);
        };
    }
}
