package de.uni.osse.ma.rs.dto;

public enum ColumnType {
    IDENTIFIER,
    PSEUDO_IDENTIFIER,
    // a potential classification target
    CLASSIFICATION_TARGET,
    // no special treatment necessary. This includes lab results and anything else
    UNDEFINED
}
