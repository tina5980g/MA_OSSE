package de.uni.osse.ma.service.simmulatedAnnealing.fields.enums;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.Obfuscutable;

public enum Country implements Obfuscutable {
    UNKNOWN("*"),
    CAMBODIA("Asia"),
    CANADA("North America"),
    CHINA("Asia"),
    COLUMBIA("South America"),
    CUBA("Central America"),
    DOMINICAN_REPUBLIC("Central America"),
    ECUADOR("Africa"),
    EL_SALVADOR("Central America"),
    ENGLAND("Europe"),
    FRANCE("Europe"),
    GERMANY("Europe"),
    GREECE("Europe"),
    GUATEMALA("Central America"),
    HAITI("Central America"),
    HOLAND_NETHERLANDS("Europe"),
    HONDURAS("Central America"),
    HONG_KONG("Asia"),
    HUNGARY("Europe"),
    INDIA("Asia"),
    IRAN("Asia"),
    IRELAND("Europe"),
    ITALY("Europe"),
    JAMAICA("Central America"),
    JAPAN("Asia"),
    LAOS("Asia"),
    MEXICO("Central America"),
    NICARAGUA("Central America"),
    UNITED_STATES_OUTER("Oceania"),
    PERU("South America"),
    PHILIPPINES("Asia"),
    POLAND("Europe"),
    PORTUGAL("Europe"),
    PUERTO_RICO("Central America"),
    SCOTLAND("Europe"),
    SOUTH_KOREA("Asia"),
    TAIWAN("Asia"),
    THAILAND("Asia"),
    TRINIDAD_AND_TOBAGO("Central America"),
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
