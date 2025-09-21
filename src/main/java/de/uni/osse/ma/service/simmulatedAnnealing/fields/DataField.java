package de.uni.osse.ma.service.simmulatedAnnealing.fields;


public abstract class DataField<T> implements Obfuscatable {
    protected T internalValue;

    public DataField(String rawValue) {
        this.internalValue = parse(rawValue);
    }

    /** deferred initialization */
    protected DataField() {
    }

    protected abstract T parse(String rawValue);
}
