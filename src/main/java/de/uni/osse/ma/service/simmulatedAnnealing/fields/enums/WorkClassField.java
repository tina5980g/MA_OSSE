package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscatable;

public class WorkClassField extends EnumField<WorkClassField.WorkClass> {

    public WorkClassField(String rawValue) {
        super(rawValue,  WorkClass.class);
    }

    enum WorkClass implements Obfuscatable, ObfuscationLevelRepresentation {
        PRIVATE(WorkClassLevel1.NON_GOVERNMENT),
        SELF_EMP_NOT_INC(WorkClassLevel1.NON_GOVERNMENT),
        SELF_EMP_INC(WorkClassLevel1.NON_GOVERNMENT),
        FEDERAL_GOV(WorkClassLevel1.GOVERNMENT),
        LOCAL_GOV(WorkClassLevel1.GOVERNMENT),
        STATE_GOV(WorkClassLevel1.GOVERNMENT),
        WITHOUT_PAY(WorkClassLevel1.UNEMPLOYED),
        NEVER_WORKED(WorkClassLevel1.UNEMPLOYED),
        ;

        private final ObfuscationLevelRepresentation nextLevel;

        WorkClass(ObfuscationLevelRepresentation nextLevel) {
            this.nextLevel = nextLevel;
        }

        @Override
        public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
            if (level == 0) return this.name();
            if (level > 2) throw new NoMoreAnonymizationLevelsException(level);

            ObfuscationLevelRepresentation currLevel = this;
            for (int i = 1; i <= level; i++) {
                currLevel = currLevel.getNextLevel();
            }
            return currLevel.toString();
        }

        @Override
        public ObfuscationLevelRepresentation getNextLevel() {
            return nextLevel;
        }
    }

    enum WorkClassLevel1 implements ObfuscationLevelRepresentation {
        // level 1
        NON_GOVERNMENT,
        GOVERNMENT,
        UNEMPLOYED
        ;

        private final ObfuscationLevelRepresentation nextLevel = StaticRepresentations.LAST_LEVEL;

        @Override
        public ObfuscationLevelRepresentation getNextLevel() {
            return nextLevel;
        }
    }
}
