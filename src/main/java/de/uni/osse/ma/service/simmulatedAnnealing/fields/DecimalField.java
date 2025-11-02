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
    public String representWithObfuscation(int level, Map<String, Object> params) throws NoMoreAnonymizationLevelsException {
        if (level == 0) {
            return this.internalValue.toString();
        }

        int maxLevel = 1;
        if (this.params.containsKey(PARAM_KEY_INTERVALS)) {
            maxLevel = ((List<?>) this.params.get(PARAM_KEY_INTERVALS)).size() + 1;
        }

        if (level == maxLevel) {
            return "*";
        }
        if (level > maxLevel) {
            throw new NoMoreAnonymizationLevelsException(level);
        }

        if (MapUtils.isNotEmpty(this.params)) {
            if (params.containsKey(PARAM_KEY_INTERVALSTART) && this.internalValue.compareTo(new BigDecimal(MapUtils.getString(params, PARAM_KEY_INTERVALSTART))) < 0) {
                return "<" + MapUtils.getString(params, PARAM_KEY_INTERVALSTART);
            }
            if (params.containsKey(PARAM_KEY_INTERVALEND) && this.internalValue.compareTo(new BigDecimal(MapUtils.getString(params, PARAM_KEY_INTERVALEND))) >= 0) {
                return ">=" + MapUtils.getString(params, PARAM_KEY_INTERVALEND);
            }
            if (params.containsKey(PARAM_KEY_INTERVALS)) {
                // should always be true, since maxLevel is only greater 1 if we got intervals
                BigDecimal intervalSize = new BigDecimal(MapUtils.getString(params, PARAM_KEY_INTERVALS));
                BigDecimal intervalStart = BigDecimal.ZERO;
                if (params.containsKey(PARAM_KEY_INTERVALSTART)) {
                    intervalStart = new BigDecimal(MapUtils.getString(params, PARAM_KEY_INTERVALSTART));
                }
                BigDecimal intervalFloor = this.internalValue.subtract(intervalStart).divide(intervalSize, RoundingMode.FLOOR).multiply(intervalSize);
                BigDecimal intervalCeil = intervalFloor.add(intervalSize);

                return intervalFloor + "-" + intervalCeil;
            }
        }
        throw new NoMoreAnonymizationLevelsException(level);
    }
}
