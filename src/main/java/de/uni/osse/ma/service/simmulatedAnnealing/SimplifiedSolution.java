package de.uni.osse.ma.service.simmulatedAnnealing;


import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

public record SimplifiedSolution(BigDecimal score, Map<String, Integer> columnIdentifierToLevel) {
    public SimplifiedSolution(Solution fullSolution) {
        this(fullSolution.getScore(),
                fullSolution.getAnonymityLevels().entrySet()
                        .stream()
                        .collect(Collectors.toMap(entry -> entry.getKey().columnIdentifier(), Map.Entry::getValue)));
    }
}
