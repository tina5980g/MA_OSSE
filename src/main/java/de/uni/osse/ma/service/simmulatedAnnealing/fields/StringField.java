package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

import java.util.List;
import java.util.Map;

public class StringField extends DataField<String> {
    private static final String PARAM_KEY_MAPPING = "mapping";

    public StringField(String rawValue) {
        super(rawValue);
    }

    @Override
    protected String parse(String rawValue) {
        return rawValue;
    }

    @Override
    public int getDynamicMaxObfuscation(Map<String, Object> params) {
        if (params.isEmpty()) {
            return 1;
        }
        List<?> mappings = (List<?>) params.getOrDefault(PARAM_KEY_MAPPING, List.of());
        return mappings.size() +1;
    }

    @Override
    public String representWithObfuscation(int level, Map<String, Object> params) throws NoMoreAnonymizationLevelsException {
        if (level == 0) {
            return this.internalValue;
        }

        int maxLevel = getDynamicMaxObfuscation(params);
        List<Map<String, String>> mappings = (List<Map<String, String>>) params.getOrDefault(PARAM_KEY_MAPPING, List.of());
        if (level <= mappings.size()) {
            Map<String, String> mapping = mappings.get(level-1);
            return mapping.get(this.internalValue);
        }
        if (level == maxLevel) {
            return "*";
        }
        throw new NoMoreAnonymizationLevelsException(level);
    }
}
