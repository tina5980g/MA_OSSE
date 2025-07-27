package de.uni.osse.ma.service.simmulatedAnnealing;

import lombok.Getter;

@Getter
public enum FILE_TYPE {
    DATA_SET("data.csv"),
    HEADER_DATA("headerdata.json"),
    PROCESSED_DATA_SET("processed.csv"),
    ;

    private final String filename;

    FILE_TYPE(String filename) {
        this.filename = filename;
    }
}
