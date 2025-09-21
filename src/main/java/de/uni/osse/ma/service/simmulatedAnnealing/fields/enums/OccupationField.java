package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscatable;

public class OccupationField extends EnumField<OccupationField.Occupation> {

    public OccupationField(String rawValue) {
        super(rawValue,  Occupation.class);
    }

    enum Occupation implements Obfuscatable, ObfuscationLevelRepresentation {
        TECH_SUPPORT(Occupationlevel1.TECHNICAL),
        CRAFT_REPAIR(Occupationlevel1.TECHNICAL),
        OTHER_SERVICE(Occupationlevel1.OTHER),
        SALES(Occupationlevel1.NONTECHNICAL),
        EXEC_MANAGERIAL(Occupationlevel1.NONTECHNICAL),
        PROF_SPECIALTY(Occupationlevel1.TECHNICAL),
        HANDLERS_CLEANERS(Occupationlevel1.NONTECHNICAL),
        MACHINE_OP_INSPCT(Occupationlevel1.TECHNICAL),
        ADM_CLERICAL(Occupationlevel1.OTHER),
        FARMING_FISHING(Occupationlevel1.OTHER),
        TRANSPORT_MOVING(Occupationlevel1.OTHER),
        PRIV_HOUSE_SERV(Occupationlevel1.OTHER),
        PROTECTIVE_SERV(Occupationlevel1.OTHER),
        ARMED_FORCES(Occupationlevel1.OTHER),
        ;

        private final Occupationlevel1 nextLevel;

        Occupation(Occupationlevel1 nextLevel) {
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

    enum Occupationlevel1 implements ObfuscationLevelRepresentation {
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

