package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

// TODO: only allows up to level 2. I dont think static DataFields are enough anymore
public class DashDelimited extends StringField {

    public DashDelimited(String rawValue) {
        super(rawValue);
    }

    @Override
    public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
        if (level == 0) return internalValue;
        String[] split = internalValue.split("-");
        if (split.length == 0) return "*";
        if (level == 1) return split[0];
        if (level == 2) return "*";

        return "";
    }
}
