package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

import java.util.Map;

public class YearField extends IntegerField {
    public YearField(String rawValue) {
        super(rawValue);
    }

    @Override
    public String representWithObfuscation(int level, Map<String, Object> params) throws NoMoreAnonymizationLevelsException {
        return switch (level) {
            case 0 -> this.internalValue.toString();
            case 1 -> intoInterval(5);
            case 2 -> intoInterval(10);
            case 3 -> intoInterval(20);
            case 4 -> "*";
            default -> throw new NoMoreAnonymizationLevelsException(level);
        };
    }
}
