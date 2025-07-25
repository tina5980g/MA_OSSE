package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public class IntegerField extends DataField<Integer> {

    public IntegerField(String rawValue) {
        super(rawValue);
    }

    @Override
    protected Integer parse(String rawValue) {
        return Integer.valueOf(rawValue);
    }

    @Override
    public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
        if (level == 0) {
            return this.internalValue.toString();
        } else {
            return intoInterval((int) Math.pow(10, level)); // since we will always be positive
        }
    }

    protected String intoInterval(int intervalSize) {
        int lowerBound = (this.internalValue - (this.internalValue % intervalSize));
        return lowerBound + " - " + (lowerBound + intervalSize);
    }
}
