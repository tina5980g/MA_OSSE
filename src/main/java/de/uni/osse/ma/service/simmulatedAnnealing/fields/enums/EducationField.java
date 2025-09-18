package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscutable;

import static de.uni.osse.ma.service.simmulatedAnnealing.fields.enums.EducationField.Education.EducationLevels2.*;

public class EducationField extends EnumField<EducationField.Education> {

    public EducationField(String rawValue) {
        super();
        this.clazz = Education.class;

        String parsingValue = rawValue;
        if (Character.isDigit(parsingValue.charAt(0))) parsingValue = "Y" + parsingValue;
        parsingValue = parsingValue.toUpperCase().replaceAll("-", "_");
        // additional preprocessing, since enums can't start with digits
        this.internalValue = parse(parsingValue);
    }

    enum Education implements Obfuscutable, ObfuscationLevelRepresentation {
        BACHELORS(EducationLevels1.UNDERGRADUATE),
        SOME_COLLEGE(EducationLevels1.UNDERGRADUATE),
        Y11TH(EducationLevels1.HIGH_SCHOOL),
        HS_GRAD(EducationLevels1.HIGH_SCHOOL),
        PROF_SCHOOL(EducationLevels1.PROFESSIONAL_EDUCATION),
        ASSOC_ACDM(EducationLevels1.PROFESSIONAL_EDUCATION),
        ASSOC_VOC(EducationLevels1.PROFESSIONAL_EDUCATION),
        Y9TH(EducationLevels1.HIGH_SCHOOL),
        Y7TH_8TH(EducationLevels1.HIGH_SCHOOL),
        Y12TH(EducationLevels1.HIGH_SCHOOL),
        MASTERS(EducationLevels1.GRADUATE),
        Y1ST_4TH(EducationLevels1.PRIMARY_SCHOOL),
        Y10TH(EducationLevels1.HIGH_SCHOOL),
        DOCTORATE(EducationLevels1.GRADUATE),
        Y5TH_6TH(EducationLevels1.PRIMARY_SCHOOL),
        PRESCHOOL(EducationLevels1.PRIMARY_SCHOOL)
        // not included in ARX' hierarchies,
        ;

        private final ObfuscationLevelRepresentation nextLevel;

        Education(ObfuscationLevelRepresentation nextLevel) {
            this.nextLevel = nextLevel;
        }


        @Override
        public ObfuscationLevelRepresentation getNextLevel() {
            return nextLevel;
        }

        @Override
        public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
            if (level == 0) return this.name();
            if (level > 3) throw new NoMoreAnonymizationLevelsException(level);

            ObfuscationLevelRepresentation currLevel = this;
            for (int i = 1; i <= level; i++) {
                currLevel = currLevel.getNextLevel();
            }
            return currLevel.toString();
        }

        enum EducationLevels1 implements ObfuscationLevelRepresentation {
            // level 1
            UNDERGRADUATE(HIGHER_EDUCATION),
            HIGH_SCHOOL(SECONDARY_EDUCATION),
            PROFESSIONAL_EDUCATION(HIGHER_EDUCATION),
            GRADUATE(HIGHER_EDUCATION),
            PRIMARY_SCHOOL(PRIMARY_EDUCATION)
            ;

            private final ObfuscationLevelRepresentation nextLevel;

            EducationLevels1(ObfuscationLevelRepresentation nextLevel) {
                this.nextLevel = nextLevel;
            }


            @Override
            public ObfuscationLevelRepresentation getNextLevel() {
                return nextLevel;
            }
        }

        enum EducationLevels2 implements ObfuscationLevelRepresentation {
            HIGHER_EDUCATION(StaticRepresentations.LAST_LEVEL),
            SECONDARY_EDUCATION(StaticRepresentations.LAST_LEVEL),
            PRIMARY_EDUCATION(StaticRepresentations.LAST_LEVEL)
            ;

            private final ObfuscationLevelRepresentation nextLevel;

            EducationLevels2(ObfuscationLevelRepresentation nextLevel) {
                this.nextLevel = nextLevel;
            }

            @Override
            public ObfuscationLevelRepresentation getNextLevel() {
                return nextLevel;
            }
        }
    }

}
