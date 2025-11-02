package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.rs.dto.HeadersDto;

import java.math.BigDecimal;

public interface ProcessorParams {
    String dataIdentifier();
    HeadersDto headerSource();
    String classificationTarget();
    Integer kLevel();
    BigDecimal maxSuppression();
}
