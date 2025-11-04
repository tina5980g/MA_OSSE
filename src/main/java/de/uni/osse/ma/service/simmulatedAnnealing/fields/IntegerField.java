package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import org.apache.commons.collections4.MapUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class IntegerField extends DataField<Integer> {
    private static final String PARAM_KEY_INTERVALSTART = "intervalStart";
    private static final String PARAM_KEY_INTERVALEND = "intervalEnd";
    private static final String PARAM_KEY_INTERVALS = "intervals";

    public IntegerField(String rawValue) {
        super(rawValue);
    }

    @Override
    protected Integer parse(String rawValue) {
        return new BigDecimal(rawValue).intValue();
    }

    @Override
    public int getDynamicMaxObfuscation(Map<String, Object> params) {
        if (!params.isEmpty() && params.containsKey(PARAM_KEY_INTERVALS)) {
            return ((List<?>) params.get(PARAM_KEY_INTERVALS)).size() + 1;
        }
        return super.getDynamicMaxObfuscation(params);
    }

    @Override
    public String representWithObfuscation(int level, Map<String, Object> params) throws NoMoreAnonymizationLevelsException {
        if (level == 0) {
            return this.internalValue.toString();
        }

        final int maxLevel = getDynamicMaxObfuscation(params);

        if (level == maxLevel) {
            return "*";
        }
        if (level > maxLevel) {
            throw new NoMoreAnonymizationLevelsException(level);
        }

        int intervalStart = 0;
        int intervalEnd = Integer.MAX_VALUE;
        int intervalSize = (int) Math.pow(10, level);

        if (MapUtils.isNotEmpty(params)) {
            if (params.containsKey(PARAM_KEY_INTERVALSTART)) {
                intervalStart = Integer.decode(MapUtils.getString(params, PARAM_KEY_INTERVALSTART));
            }
            if (params.containsKey(PARAM_KEY_INTERVALEND)) {
                intervalEnd = Integer.decode(MapUtils.getString(params, PARAM_KEY_INTERVALEND));
            }
            if (params.containsKey(PARAM_KEY_INTERVALS)) {
                // should always be true, since maxLevel is only greater 1 if we got intervals
                intervalSize = Integer.decode(((List<?>) params.get(PARAM_KEY_INTERVALS)).get(level-1).toString());
            }
        }

        return toInterval(intervalStart, intervalSize, intervalEnd); // since we will always be positive
    }

    protected String toInterval(int intervalStart, int intervalSize, int intervalEnd) {
        if (this.internalValue.compareTo(intervalStart) < 0) {
            return "<" + intervalStart;
        }
        if (this.internalValue.compareTo(intervalEnd) >= 0) {
            return ">=" + intervalEnd;
        }
        // int arithmetics automatically "rounds down" by dropping the decimals
        int intervalFloor = ((this.internalValue - intervalStart)/intervalSize) * intervalSize;
        int intervalCeil = intervalFloor + intervalSize;

        return intervalFloor + "-" + intervalCeil;
    }
}
