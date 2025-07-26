package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.exceptions.NoMoreIterationsException;
import de.uni.osse.ma.rs.dto.HeaderInfo;
import de.uni.osse.ma.rs.dto.HeadersDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.annotation.SessionScope;


import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** runs simmulated annealing by running multiple categorizers to find a local optimum */
@SessionScope // TODO: correct type?
@Slf4j
public class SimmulatedAnnealing {
    private static final Random RANDOM = new Random();

    private final Categorizer categorizer;
    private final String dataSource;
    private final String classificationTarget;
    // TODO: configurable
    private final int findSolutionMaxRetries = 10;

    private List<HeaderInfo> keyList; // For easier random access
    private Set<Solution> history = new HashSet<>();

    // TODO: mixing state with injection?
    public SimmulatedAnnealing(HeadersDto headerSource, String dataSource, String classificationTarget, Categorizer categorizer) {
        this.dataSource = dataSource;
        this.classificationTarget = classificationTarget;
        this.categorizer = categorizer;
        this.categorizer.afterPropertiesSet();

        keyList = headerSource.columns();
    }

    public Solution calcLocalOptimumSolution() throws Exception {
        // TODO: remove previous score-file, if it exists
        // initial Solution
        Solution nextSolution = new Solution(keyList.stream().collect(Collectors.toMap(
                Function.identity(),                        // key
                headerInfo -> RANDOM.nextInt(((HeaderInfo) headerInfo).dataType().getMaxObfuscation() + 1),     // value
                (key1, key2) -> key1,        // conflict merger, dummy
                HashMap::new)                               // supplier
        ));

        Solution currentSolution = nextSolution;
        Solution allTimeBestSolution = nextSolution;

        double currentTemperature = 1000;
        final double minTemperature = 0.001;
        final double coolingRate = 0.01;

        int maxIterationsWithoutImprovement = 8;
        int iterationsWithoutImprovement = 0;

        while(currentTemperature > minTemperature && iterationsWithoutImprovement < maxIterationsWithoutImprovement) {
            boolean hasChanged = false;

            BigDecimal score = categorizer.scoreModelAccurary(new Categorizer.ClassificationScriptArguments.ClassificationScriptArgumentsBuilder()
                    .datasetFilename(dataSource)
                    .equivalenceclassSize(3)
                    .maxSuppression(0.001)
                    .targetColumn(classificationTarget)
                    .solutionColumns(solutionToPythonArg(nextSolution))
            );

            nextSolution.setScore(score);
            history.add(nextSolution);

            // is this an improvement?
            // For the first iteration, we compare the initial solution with itself. Just a nice sideeffect
            if (nextSolution.getScore().compareTo(currentSolution.getScore()) >= 0) {
                currentSolution = nextSolution;
                if (nextSolution.getScore().compareTo(allTimeBestSolution.getScore()) >= 0) {
                    allTimeBestSolution = nextSolution;
                }
                hasChanged = true;
            } else {
                double acceptance_probability = Math.exp((currentSolution.getScore().subtract(nextSolution.getScore())).doubleValue() / currentTemperature);
                if (RANDOM.nextDouble() < acceptance_probability) {
                    currentSolution = nextSolution;
                    hasChanged = true;
                }
            }

            if (hasChanged) {
                iterationsWithoutImprovement = 0;
                currentTemperature = currentTemperature * coolingRate;
            } else {
                iterationsWithoutImprovement++;
            }

            // find next candidate
            try {
                nextSolution = nextSolution(currentSolution);
            } catch (NoMoreIterationsException e) {
                log.error("Couldn't find an unscored neighbor.", e);
                break;
            }
        }

        return allTimeBestSolution;
    }

    private List<String> solutionToPythonArg(Solution solution) {
        return solution.getAnonymityLevels().entrySet().stream().map(entry ->
            entry.getKey().columnName() + "_" + entry.getValue()
        ).toList();
    }

    private Solution nextSolution(Solution currentSolution) throws NoMoreIterationsException {
        HeaderInfo headerInfoToChange = keyList.get(RANDOM.nextInt(keyList.size()));
        for(int i = 0; i< findSolutionMaxRetries; i++) {
            Integer newObfuscation = RANDOM.nextInt(headerInfoToChange.dataType().getMaxObfuscation());
            // this guarantees that we don't get the same obfuscation again
            if (newObfuscation >= currentSolution.getAnonymityLevels().get(headerInfoToChange)) {
                newObfuscation++;
            }
            // next Solution is currentSolution, with one level changed
            final Solution nextSolution = new Solution(currentSolution.getAnonymityLevels());
            nextSolution.getAnonymityLevels().put(headerInfoToChange, newObfuscation);

            // TODO: is it even worth it (and in the spirit of simulated annealing) to keep track of this?
            if (history.stream().anyMatch(x -> x.getAnonymityLevels().equals(nextSolution.getAnonymityLevels()))) {
                log.warn("Duplicate Solution {}", nextSolution.getAnonymityLevels());
            } else {
                return nextSolution;
            }
        }
        throw new NoMoreIterationsException();
    }
}
