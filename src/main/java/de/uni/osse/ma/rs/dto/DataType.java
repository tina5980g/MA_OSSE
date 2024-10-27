package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.*;
import lombok.Getter;

@Getter
public enum DataType {
    IDENTIFIER(IdentifyingField.class,0),
    NONIDENTIFIER(StringField.class, 1),
    STRING(StringField.class, 1),
    YEAR(YearField.class, 3),
    DATE(DateField.class, 4);

    private final Class<? extends DataField<?>> representingClass;
    private final int maxObfuscation;

    DataType(Class<? extends DataField<?>> representingClass, final int maxObfuscation) {
        this.representingClass = representingClass;
        this.maxObfuscation = maxObfuscation;
    }
}
