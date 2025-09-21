package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscatable;

public class RaceField extends EnumField<RaceField.Race> {

    public RaceField(String rawValue) {
        super(rawValue, RaceField.Race.class);
    }

    enum Race implements Obfuscatable {
        WHITE,
        ASIAN_PAC_ISLANDER,
        AMER_INDIAN_ESKIMO,
        OTHER,
        BLACK;

        @Override
        public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
            return switch (level) {
                case 0 -> this.name();
                case 1 -> "*";
                default -> throw new NoMoreAnonymizationLevelsException(level);
            };
        }
    }
}
