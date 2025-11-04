package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import java.util.Map;

public abstract class DataField<T> implements Obfuscatable {
    protected T internalValue;

    public DataField(String rawValue) {
        this.internalValue = parse(rawValue);
    }

    /** deferred initialization */
    protected DataField() {
    }

    protected abstract T parse(String rawValue);

    public int getDynamicMaxObfuscation(Map<String, Object> params) {
        return 5; // upper bound. Everything can deal with NoAnonymizationExceptions.
    }
}
