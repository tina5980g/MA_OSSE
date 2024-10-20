package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.DataField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.DateField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.StringField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.YearField;
import lombok.Getter;

public enum DataType {
    STRING(StringField.class),
    YEAR(YearField.class),
    DATE(DateField.class);

    @Getter
    private final Class<? extends DataField<?>> representingClass;

    DataType(Class<? extends DataField<?>> representingClass) {
        this.representingClass = representingClass;
    }
}
