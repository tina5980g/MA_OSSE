package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;

public class CountryField extends EnumField<Country> {


    public CountryField(String rawValue) {
        super(rawValue, Country.class);
    }

    @Override
    protected Country parse(String rawValue) throws IllegalArgumentException {
        if (rawValue.equalsIgnoreCase("Outlying-US(Guam-USVI-etc)")) return Country.UNITED_STATES_OUTER;
        if (rawValue.equalsIgnoreCase("Hong")) return Country.HONG_KONG;
        if (rawValue.equalsIgnoreCase("South")) return Country.SOUTH_KOREA;
        if (rawValue.equalsIgnoreCase("Trinadad and Tobago")) return Country.TRINIDAD_AND_TOBAGO; // typo in the dataset
        if (rawValue.equalsIgnoreCase("nan") || rawValue.equalsIgnoreCase("null") || rawValue.equalsIgnoreCase("undefined")) return Country.UNKNOWN; // typo in the dataset
        return Enum.valueOf(clazz, rawValue.toUpperCase().replaceAll("-", "_"));
    }

}
