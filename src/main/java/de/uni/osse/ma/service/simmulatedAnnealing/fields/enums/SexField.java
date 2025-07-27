package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;

public class SexField extends EnumField<Sex> {
    public SexField(String rawValue) {
        super(rawValue, Sex.class);
    }
}
