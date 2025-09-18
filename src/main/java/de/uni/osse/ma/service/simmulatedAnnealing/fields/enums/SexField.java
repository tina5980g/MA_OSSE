package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscutable;

public class SexField extends EnumField<SexField.Sex> {
    public SexField(String rawValue) {
        super(rawValue, SexField.Sex.class);
    }

    public enum Sex implements Obfuscutable {
        MALE,
        FEMALE
        ;


        @Override
        public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
            if (level == 0) return this.name();
            if (level == 1) return "*";
            throw new NoMoreAnonymizationLevelsException(level);
        }
    }
}
