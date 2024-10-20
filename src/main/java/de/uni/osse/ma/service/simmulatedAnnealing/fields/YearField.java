package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public class YearField extends DataField<Integer> {
    public YearField(String rawValue) {
        super(rawValue);
    }

    @Override
    protected Integer parse(String rawValue) {
        return Integer.valueOf(rawValue);
    }

    @Override
    public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
        return switch (level) {
            case 0 -> this.internalValue.toString();
            case 1 -> {
                int lowerBound = (this.internalValue - (this.internalValue % 5));
                yield lowerBound + " - " + (lowerBound + 5);
            }
            case 2 -> {
                int lowerBound = (this.internalValue - (this.internalValue % 10));
                yield lowerBound + " - " + (lowerBound + 10);
            }
            case 3 -> "*";
            default -> throw new NoMoreAnonymizationLevelsException(level);
        };
    }
}
