package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.rs.dto.HeaderInfo;
import de.uni.osse.ma.rs.dto.HeadersDto;
import de.uni.osse.ma.rs.dto.ObfuscationInfo;
import de.uni.osse.ma.service.FileInteractionService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * runs simulated annealing by running multiple categorizers to find a local optimum
 */
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
    public void calcLocalOptimumSolution(Parameters parameters) throws Exception {
        final Set<Solution> history = new HashSet<>();
        final String dataSource = fileInteractionService.getPathToFile(parameters.dataIdentifier(), FILE_TYPE.PROCESSED_DATA_SET).toString();

        final List<HeaderInfo> keyList = parameters.headerSource().columns().stream() // For easier random access
                // IDENTIFIER can not be reasonably obfuscated, if they are part of the dataset
                .filter(headerInfo -> parameters.headerSource().maxObfuscationFor(headerInfo) > 0)
                // classificationTarget is not a valid source for further obfuscations
                .filter(headerInfo -> !parameters.classificationTarget().contains(headerInfo.columnName()))
                // PROVIDED strategy results in multiple HeaderInfo for the same columnName. We select the one with level == 0
                // With Strategy PROVIDED, an obfuscationLevel must exist (see the constructor of ObfuscationInfo)
                .filter(headerInfo -> headerInfo.obfuscationInfo().strategy() != ObfuscationInfo.ObfuscationStrategy.PROVIDED || headerInfo.obfuscationInfo().level() <= 0)
                .toList();


        // initial Solution
        Solution currentSolution = new Solution(keyList.stream().collect(Collectors.toMap(
                Function.identity(),                        // key
                headerInfo -> RANDOM.nextInt(parameters.headerSource().maxObfuscationFor(headerInfo) + 1),     // value
                (key1, _) -> key1,        // conflict merger, dummy
                HashMap::new)                               // supplier
        ));
        Solution allTimeBestSolution = currentSolution;

        double currentTemperature = 1000;
        final double minTemperature = 0.001;
        final double coolingRate = 0.8;

        int maxIterationsWithoutImprovement = 8;
        int iterationsWithoutImprovement = 0;

        while (currentTemperature > minTemperature && iterationsWithoutImprovement < maxIterationsWithoutImprovement) {
            boolean hasChanged = false;

            // find next candidate
            final Solution nextSolution = history.isEmpty() ? currentSolution : nextSolution(parameters.headerSource(), currentSolution, keyList);
            final Optional<Solution> historicalSolution = history.stream()
                    .filter(sol -> Objects.equals(sol.getAnonymityLevels(), nextSolution.getAnonymityLevels()))
                    .findAny(); // there should only ever be one match
            if (historicalSolution.isPresent()) {
                log.warn("Duplicate Solution {}. Retrieved score {} from history.", nextSolution.getAnonymityLevels(), historicalSolution.get().getScore());
                // this solution was already scored. To save on computation time, we don't calculate the score again
                nextSolution.setScore(historicalSolution.get().getScore());
            } else if (nextSolution.getAnonymityLevels().entrySet().stream().allMatch(headerInfoIntegerEntry ->
                    headerInfoIntegerEntry.getValue() == (parameters.headerSource().maxObfuscationFor(headerInfoIntegerEntry.getKey()))
            )) {
                // all maximum obfuscations -> CatBoost will error
                nextSolution.setScore(BigDecimal.ONE.negate());
                history.add(nextSolution);

            } else {
                BigDecimal score = categorizer.scoreModelAccurary(new Categorizer.ClassificationScriptArguments.ClassificationScriptArgumentsBuilder()
                        .rootPath(fileInteractionService.getRootPath())
                        .datasetFilename(dataSource)
                        .equivalenceclassSize(parameters.kLevel())
                        .maxSuppression(parameters.maxSuppression())
                        .targetColumn(parameters.classificationTarget())
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

        fileInteractionService.writeSolution(parameters.dataIdentifier(), parameters.solutionIdentifier(), new SimplifiedSolution(allTimeBestSolution));
        final long totalSolutions = keyList.stream()
                .map(info -> parameters.headerSource().maxObfuscationFor(info.columnName()) + 1)
                .reduce((a, b) -> a * b).orElse(1);
        log.info("DONE! Scored {} out of {} Solutions ({}%)", history.size(), totalSolutions, ((double) history.size()) / totalSolutions);
    }

    private List<String> solutionToPythonArg(Solution solution) {
        return solution.getAnonymityLevels().entrySet().stream().map(entry ->
                entry.getKey().columnName() + "_" + entry.getValue()
        ).toList();
    }

    private Solution nextSolution(HeadersDto headerSource, Solution currentSolution, final List<HeaderInfo> keyList) {
        HeaderInfo headerInfoToChange = keyList.get(RANDOM.nextInt(keyList.size()));
        Integer newObfuscation = RANDOM.nextInt(headerSource.maxObfuscationFor(headerInfoToChange));
        // this guarantees that we don't get the same obfuscation again
        if (newObfuscation >= currentSolution.getAnonymityLevels().get(headerInfoToChange)) {
            newObfuscation++;
        }
        // next Solution is currentSolution, with one level changed
        final Solution nextSolution = new Solution(currentSolution.getAnonymityLevels());
        nextSolution.getAnonymityLevels().put(headerInfoToChange, newObfuscation);

        return nextSolution;
    }

    public record Parameters(@Nonnull String dataIdentifier, @Nonnull HeadersDto headerSource,
                             @Nonnull String solutionIdentifier, @Nonnull String classificationTarget,
                             @Nonnull Integer kLevel, @Nonnull BigDecimal maxSuppression) {

        @Builder
        public Parameters(@Nonnull String dataIdentifier, @Nonnull HeadersDto headerSource, @Nonnull String solutionIdentifier, @Nonnull String classificationTarget, @Nullable Integer kLevel, @Nullable BigDecimal maxSuppression) {
            this.dataIdentifier = dataIdentifier;
            this.headerSource = headerSource;
            this.solutionIdentifier = solutionIdentifier;
            this.classificationTarget = classificationTarget;
            this.kLevel = Objects.requireNonNullElse(kLevel, 2);
            this.maxSuppression = Objects.requireNonNullElse(maxSuppression, BigDecimal.valueOf(1, 1));
        }
    }
}
