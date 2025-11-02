package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DateField extends DataField<LocalDate> {
    private static final DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter NO_DAY_FORMAT = DateTimeFormatter.ofPattern("MM.yyyy");
    private static final DateTimeFormatter ONLY_YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");

    public DateField(String rawValue) {
        super(rawValue);
    }

    @Override
    protected LocalDate parse(String rawValue) {
        return LocalDate.parse(rawValue);
    }

    @Override
    public String representWithObfuscation(int level, Map<String, Object> params) throws NoMoreAnonymizationLevelsException {
        return switch (level) {
            case 0 -> internalValue.format(FULL_DATE_FORMAT);
            case 1 -> internalValue.format(NO_DAY_FORMAT);
            case 2 -> internalValue.format(ONLY_YEAR_FORMAT);
            case 3 -> {
                int year = internalValue.getYear();
                int lowerBound = (year - (year % 5));
                yield lowerBound + " - " + (lowerBound + 5);
            }
            case 4 -> {
                int year = internalValue.getYear();
                int lowerBound = (year - (year % 10));
                yield lowerBound + " - " + (lowerBound + 10);
            }

            default -> throw new NoMoreAnonymizationLevelsException(level);
        };
    }
}
