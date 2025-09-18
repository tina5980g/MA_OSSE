package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscutable;

public class MaritalStatusField extends EnumField<MaritalStatusField.MaritalStatus> {
    public MaritalStatusField(String rawValue) {
        super(rawValue, MaritalStatusField.MaritalStatus.class);
    }

    public enum MaritalStatus implements Obfuscutable {
        MARRIED_CIV_SPOUSE("Spouse present"),
        DIVORCED("Spouse not present"),
        NEVER_MARRIED("Spouse not present"),
        SEPARATED("Spouse not present"),
        WIDOWED("Spouse not present"),
        MARRIED_SPOUSE_ABSENT("Spouse not present"),
        MARRIED_AF_SPOUSE("Spouse present"),
        ;

        final String level1;

        MaritalStatus(String level1) {
            this.level1 = level1;
        }

        @Override
        public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
            return switch (level) {
                case 0 -> this.name();
                case 1 -> this.level1;
                case 2 -> "*";
                default -> throw new NoMoreAnonymizationLevelsException(level);
            };
        }
    }

}
