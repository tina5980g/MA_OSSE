package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.*;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.enums.*;
import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PACKAGE)
public enum DataType {
    IDENTIFIER(IdentifyingField.class,0),
    NONIDENTIFIER(StringField.class, 1),
    STRING(StringField.class, 1),
    YEAR(YearField.class, 4),
    DATE(DateField.class, 4),
    INT_AMOUNT_CURRENCY(IntegerField.class, 6),
    INT_AMOUNT_COUNTER(IntegerField.class, 2),
    SEX(SexField.class, 1),
    OCCUPATION(DashDelimited.class, 2),
    IGNORE(IdentifyingField.class, 0),
    COUNTRY(CountryField.class, 2),
    // For the ARX example
    ARX_RACE(RaceField.class, 1),
    ARX_MARITAL(MaritalStatusField.class, 2),
    ARX_EDUCATION(EducationField.class, 3),
    ARX_WORKCLASS(WorkClassField.class, 2),
    ARX_OCCUPATION(OccupationField.class, 2),
    ;

    private final Class<? extends DataField<?>> representingClass;
    private final int maxObfuscation;

    DataType(Class<? extends DataField<?>> representingClass, final int maxObfuscation) {
        this.representingClass = representingClass;
        this.maxObfuscation = maxObfuscation;
    }
}
