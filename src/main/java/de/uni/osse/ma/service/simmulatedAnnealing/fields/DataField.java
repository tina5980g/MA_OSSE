package de.uni.osse.ma.service.simmulatedAnnealing.fields;


import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public abstract class DataField<T> {
    protected final T internalValue;

    public DataField(String rawValue) {
        this.internalValue = parse(rawValue);
    }

    protected abstract T parse(String rawValue);
    public abstract String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException;
}
