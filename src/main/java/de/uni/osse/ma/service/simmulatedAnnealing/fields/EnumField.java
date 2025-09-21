package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

public class EnumField<T extends Enum<T> & Obfuscatable> extends DataField<T> {
    protected Class<T> clazz;

    public EnumField(String rawValue, Class<T> clazz) {
        super();
        this.clazz = clazz;
        this.internalValue = parse(rawValue);
    }

    /* Deferred initialization */
    public EnumField() {
    }

    protected T parse(String rawValue) throws IllegalArgumentException {
        return Enum.valueOf(clazz, rawValue.toUpperCase().replaceAll("-", "_"));
    }

    @Override
    public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
        return internalValue.representWithObfuscation(level);
    }
}
