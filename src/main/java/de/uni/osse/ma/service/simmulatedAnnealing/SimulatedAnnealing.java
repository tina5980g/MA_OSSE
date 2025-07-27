package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.rs.dto.HeaderInfo;
import de.uni.osse.ma.rs.dto.HeadersDto;
import de.uni.osse.ma.service.FileInteractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** runs simulated annealing by running multiple categorizers to find a local optimum */
@Slf4j
@Component
public class SimulatedAnnealing {
    private static final Random RANDOM = new Random();

    private final Categorizer categorizer;
    private final FileInteractionService fileInteractionService;

    @Autowired
    public SimulatedAnnealing(Categorizer categorizer, FileInteractionService fileInteractionService) {
        this.categorizer = categorizer;
        this.categorizer.afterPropertiesSet();
        this.fileInteractionService = fileInteractionService;
    }

    @Async
    // TODO: prevent multiple running for the same dataIdentifier at the same time
    public void calcLocalOptimumSolution(String dataIdentifier, HeadersDto headerSource, String classificationTarget, String solutionIdentifier) throws Exception {
        // TODO: remove previous score-file, if it exists
        final Set<Solution> history = new HashSet<>();
        final String dataSource = fileInteractionService.getPathToFile(dataIdentifier, FILE_TYPE.PROCESSED_DATA_SET).toString();

        final List<HeaderInfo> keyList = headerSource.columns().stream() // For easier random access 
                // IDENTIFIER can not be reasonably obfuscated, if they are part of the dataset
                .filter(headerInfo -> headerInfo.dataType().getMaxObfuscation() > 0)
                // classificationTarget is not a valid source for further obfuscations
                .filter(headerInfo -> !classificationTarget.contains(headerInfo.columnName()))
                .toList();


        // initial Solution
        Solution currentSolution = new Solution(keyList.stream().collect(Collectors.toMap(
                Function.identity(),                        // key
                headerInfo -> RANDOM.nextInt((headerInfo).dataType().getMaxObfuscation() + 1),     // value
                (key1, _) -> key1,        // conflict merger, dummy
                HashMap::new)                               // supplier
        ));
        Solution allTimeBestSolution = currentSolution;

        double currentTemperature = 1000;
        final double minTemperature = 0.001;
        final double coolingRate = 0.1;

        int maxIterationsWithoutImprovement = 8;
        int iterationsWithoutImprovement = 0;

        while(currentTemperature > minTemperature && iterationsWithoutImprovement < maxIterationsWithoutImprovement) {
            boolean hasChanged = false;

            // find next candidate
            final Solution nextSolution = history.isEmpty() ? currentSolution : nextSolution(currentSolution, keyList);
            final Optional<Solution> historicalSolution = history.stream()
                    .filter(sol -> Objects.equals(sol.getAnonymityLevels(), nextSolution.getAnonymityLevels()))
                    .findAny(); // there should only ever be one match
            if (historicalSolution.isPresent()) {
                log.warn("Duplicate Solution {}. Retrieved score {} from history.", nextSolution.getAnonymityLevels(), historicalSolution.get().getScore());
                // this solution was already scored. To save on computation time, we don't calculate the score again
                nextSolution.setScore(historicalSolution.get().getScore());
            } else if (nextSolution.getAnonymityLevels().entrySet().stream().allMatch(headerInfoIntegerEntry -> 
                headerInfoIntegerEntry.getValue() == headerInfoIntegerEntry.getKey().dataType().getMaxObfuscation()
            )) {
                // all maximum obfuscations -> CatBoost will error
                nextSolution.setScore(BigDecimal.ONE.negate());
                history.add(nextSolution);
                
            } else {
                BigDecimal score = categorizer.scoreModelAccurary(new Categorizer.ClassificationScriptArguments.ClassificationScriptArgumentsBuilder()
                        .datasetFilename(dataSource)
                        .equivalenceclassSize(2)
                        .maxSuppression(0.1)
                        .targetColumn(classificationTarget)
                        .solutionColumns(solutionToPythonArg(nextSolution))
                );

                nextSolution.setScore(score);
                history.add(nextSolution);
            }
            
            // For the first iteration, we compare the initial solution with itself.
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
        }

        fileInteractionService.writeSolution(dataIdentifier, solutionIdentifier, allTimeBestSolution);
        log.info("DONE!");
    }

    private List<String> solutionToPythonArg(Solution solution) {
        return solution.getAnonymityLevels().entrySet().stream().map(entry ->
            entry.getKey().columnName() + "_" + entry.getValue()
        ).toList();
    }

    private Solution nextSolution(Solution currentSolution, final List<HeaderInfo> keyList) {
        HeaderInfo headerInfoToChange = keyList.get(RANDOM.nextInt(keyList.size()));
        Integer newObfuscation = RANDOM.nextInt(headerInfoToChange.dataType().getMaxObfuscation());
        // this guarantees that we don't get the same obfuscation again
        if (newObfuscation >= currentSolution.getAnonymityLevels().get(headerInfoToChange)) {
            newObfuscation++;
        }
        // next Solution is currentSolution, with one level changed
        final Solution nextSolution = new Solution(currentSolution.getAnonymityLevels());
        nextSolution.getAnonymityLevels().put(headerInfoToChange, newObfuscation);

        return nextSolution;
    }
}
