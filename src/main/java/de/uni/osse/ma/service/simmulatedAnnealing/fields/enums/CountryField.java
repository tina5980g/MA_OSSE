package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.EnumField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscutable;

public class CountryField extends EnumField<CountryField.Country> {


    public CountryField(String rawValue) {
        super(rawValue, CountryField.Country.class);
    }

    @Override
    protected CountryField.Country parse(String rawValue) throws IllegalArgumentException {
        try {
            return super.parse(rawValue);
        } catch (IllegalArgumentException e) {
            if (rawValue.equalsIgnoreCase("Outlying-US(Guam-USVI-etc)")) return Country.UNITED_STATES_OUTER;
            if (rawValue.equalsIgnoreCase("Hong")) return Country.HONG_KONG;
            if (rawValue.equalsIgnoreCase("South")) return Country.SOUTH_KOREA;
            if (rawValue.equalsIgnoreCase("Trinadad and Tobago") || rawValue.equalsIgnoreCase("Trinadad&Tobago")) return Country.TRINIDAD_AND_TOBAGO; // typo in the dataset
            if (rawValue.equalsIgnoreCase("nan") || rawValue.equalsIgnoreCase("null") || rawValue.equalsIgnoreCase("undefined")) return Country.UNKNOWN; // typo in the dataset
            throw new IllegalArgumentException(e);
        }
    }


    public enum Country implements Obfuscutable {
        UNKNOWN("*"),
        CAMBODIA("Asia"),
        CANADA("North America"),
        CHINA("Asia"),
        COLUMBIA("South America"),
        CUBA("North America"),
        DOMINICAN_REPUBLIC("North America"),
        ECUADOR("Africa"),
        EL_SALVADOR("North America"),
        ENGLAND("Europe"),
        FRANCE("Europe"),
        GERMANY("Europe"),
        GREECE("Europe"),
        GUATEMALA("North America"),
        HAITI("North America"),
        HOLAND_NETHERLANDS("Europe"),
        HONDURAS("North America"),
        HONG_KONG("Asia"),
        HUNGARY("Europe"),
        INDIA("Asia"),
        IRAN("Asia"),
        IRELAND("Europe"),
        ITALY("Europe"),
        JAMAICA("North America"),
        JAPAN("Asia"),
        LAOS("Asia"),
        MEXICO("North America"),
        NICARAGUA("South America"),
        UNITED_STATES_OUTER("Oceania"),
        PERU("South America"),
        PHILIPPINES("Asia"),
        POLAND("Europe"),
        PORTUGAL("Europe"),
        PUERTO_RICO("North America"),
        SCOTLAND("Europe"),
        SOUTH_KOREA("Asia"),
        TAIWAN("Asia"),
        THAILAND("Asia"),
        TRINIDAD_AND_TOBAGO("South America"),
        UNITED_STATES("North America"),
        VIETNAM("Asia"),
        YUGOSLAVIA("Europe"),
        ;

        private final String continent;

        Country(String continent) {
            this.continent = continent;
        }

        @Override
        public String representWithObfuscation(int level) throws NoMoreAnonymizationLevelsException {
            return switch (level) {
                case 0 -> this.name();
                case 1 -> this.continent;
                case 2 -> "*";
                default -> throw new NoMoreAnonymizationLevelsException(level);
            };
        }
    }
}
