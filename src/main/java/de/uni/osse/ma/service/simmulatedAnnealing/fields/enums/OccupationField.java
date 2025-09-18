package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscutable;

public class OccupationField extends EnumField<OccupationField.Occupation> {

    public OccupationField(String rawValue) {
        super(rawValue,  Occupation.class);
    }

    enum Occupation implements Obfuscutable, ObfuscationLevelRepresentation {
        TECH_SUPPORT(WorkClassLevel1.TECHNICAL),
        CRAFT_REPAIR(WorkClassLevel1.TECHNICAL),
        OTHER_SERVICE(WorkClassLevel1.OTHER),
        SALES(WorkClassLevel1.NONTECHNICAL),
        EXEC_MANAGERIAL(WorkClassLevel1.NONTECHNICAL),
        PROF_SPECIALTY(WorkClassLevel1.TECHNICAL),
        HANDLERS_CLEANERS(WorkClassLevel1.NONTECHNICAL),
        MACHINE_OP_INSPCT(WorkClassLevel1.TECHNICAL),
        ADM_CLERICAL(WorkClassLevel1.OTHER),
        FARMING_FISHING(WorkClassLevel1.OTHER),
        TRANSPORT_MOVING(WorkClassLevel1.OTHER),
        PRIV_HOUSE_SERV(WorkClassLevel1.OTHER),
        PROTECTIVE_SERV(WorkClassLevel1.OTHER),
        ARMED_FORCES(WorkClassLevel1.OTHER),
        ;

        private final WorkClassLevel1 nextLevel;

        Occupation(WorkClassLevel1 nextLevel) {
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
        TECHNICAL,
        OTHER,
        NONTECHNICAL
        ;

        private final ObfuscationLevelRepresentation nextLevel = StaticRepresentations.LAST_LEVEL;

        @Override
        public ObfuscationLevelRepresentation getNextLevel() {
            return nextLevel;
        }
    }
}

