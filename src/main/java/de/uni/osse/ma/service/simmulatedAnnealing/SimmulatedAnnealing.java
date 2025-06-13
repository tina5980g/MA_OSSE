package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.rs.dto.HeaderInfo;
import de.uni.osse.ma.rs.dto.HeadersDto;


import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** runs simmulated annealing by running multiple categorizers to find a local optimum */
public class SimmulatedAnnealing {
    private static final Random RANDOM = new Random();

    private final Categorizer categorizer;
    private final String dataSource;
    private final String classificationTarget;
    private final HeadersDto headerSource;

    private Map<HeaderInfo, Integer> currentSolution;
    private List<HeaderInfo> keyList; // For easier random access

    // TODO: mixing state with injection?
    public SimmulatedAnnealing(HeadersDto headerSource, String dataSource, String classificationTarget, Categorizer categorizer) {
        this.headerSource = headerSource;
        this.dataSource = dataSource;
        this.classificationTarget = classificationTarget;
        this.categorizer = categorizer;
        this.categorizer.afterPropertiesSet();

        this.currentSolution = new HashMap<>();
        // initial Solution
        currentSolution = headerSource.columns().stream().collect(Collectors.toMap(Function.identity(), headerInfo ->
            RANDOM.nextInt(headerInfo.dataType().getMaxObfuscation() + 1)
        ));
        keyList = new ArrayList<>(currentSolution.keySet());
    }

    public void calcLocalOptimumSolution() throws Exception {

        while(true) {
            // TODO: score individual Solutions
            BigDecimal score = categorizer.scoreModelAccurary(new Categorizer.ClassificationScriptArguments.ClassificationScriptArgumentsBuilder()
                    .datasetFilename(dataSource)
                    .equivalenceclassSize(3)
                    .maxSuppression(0.001)
                    .targetColumn(classificationTarget)
                    .solutionColumns(solutionToPythonArg())
            );


            // TODO try multiple simulations adjusting parameters accordingly


            // TODO: break when reaching cooldown-threshold
            if (true) {
                break;
            }
        }

    }

    private List<String> solutionToPythonArg() {
        return this.currentSolution.entrySet().stream().map(entry ->
            entry.getKey().columnName() + "_" + entry.getValue()
        ).toList();
    }

    private void neighborSolution() {
        // TODO: prevent duplicate calculations by keeping history of solutions
        HeaderInfo headerInfoToChange = keyList.get(RANDOM.nextInt(keyList.size() + 1));
        Integer newObfuscation = RANDOM.nextInt(headerInfoToChange.dataType().getMaxObfuscation());
        // this guarantees that we don't get the same obfuscation again
        if (newObfuscation >= currentSolution.get(headerInfoToChange)) {
            newObfuscation++;
        }
        currentSolution.put(headerInfoToChange, newObfuscation);
    }
}
