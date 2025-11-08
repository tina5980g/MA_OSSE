package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.collections4.MapUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class DecimalField extends DataField<BigDecimal> {
    private static final List<String> COMMON_NULL_VALUES = List.of("nan", "null");
    private static final String PARAM_KEY_INTERVALSTART = "intervalStart";
    private static final String PARAM_KEY_INTERVALEND = "intervalEnd";
    private static final String PARAM_KEY_INTERVALS = "intervals";

    public DecimalField(String rawValue) {
        super(rawValue);
    }

    @Override
    protected BigDecimal parse(String rawValue) {
        if (StringUtils.isBlank(rawValue) || COMMON_NULL_VALUES.stream().anyMatch(val -> val.equalsIgnoreCase(rawValue))) {
            return null;
        }
        return new BigDecimal(rawValue);
    }

    @Override
    public int getDynamicMaxObfuscation(Map<String, Object> params) {
        if (params.containsKey(PARAM_KEY_INTERVALS)) {
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

        BigDecimal intervalStart = BigDecimal.ZERO;
        BigDecimal intervalEnd = BigDecimal.valueOf(Long.MAX_VALUE);
        BigDecimal intervalSize = BigDecimal.valueOf((int) Math.pow(10, level));

        if (MapUtils.isNotEmpty(params)) {
            if (params.containsKey(PARAM_KEY_INTERVALSTART)) {
                intervalStart = new BigDecimal(MapUtils.getString(params, PARAM_KEY_INTERVALSTART));
            }
            if (params.containsKey(PARAM_KEY_INTERVALEND)) {
                intervalEnd = new BigDecimal(MapUtils.getString(params, PARAM_KEY_INTERVALEND));
            }
            if (params.containsKey(PARAM_KEY_INTERVALS)) {
                // should always be true, since maxLevel is only greater 1 if we got intervalSize
                intervalSize = new BigDecimal(((List<?>) params.get(PARAM_KEY_INTERVALS)).get(level-1).toString());
            }
        }

        return toInterval(intervalStart, intervalSize, intervalEnd);
    }

    private String toInterval(BigDecimal intervalStart, BigDecimal intervalSize, BigDecimal intervalEnd) {
        if (this.internalValue.compareTo(intervalStart) < 0) {
            return "<" + intervalStart;
        }
        if (this.internalValue.compareTo(intervalEnd) >= 0) {
            return ">=" + intervalEnd;
        }
        BigDecimal intervalFloor = this.internalValue.subtract(intervalStart).divide(intervalSize, RoundingMode.FLOOR).multiply(intervalSize);
        BigDecimal intervalCeil = intervalFloor.add(intervalSize);

        return intervalFloor + "-" + intervalCeil;
    }
}
