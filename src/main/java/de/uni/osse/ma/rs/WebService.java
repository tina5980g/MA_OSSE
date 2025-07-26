package de.uni.osse.ma.rs;

import de.uni.osse.ma.rs.dto.DataWrapper;
import de.uni.osse.ma.service.FileInteractionService;
import de.uni.osse.ma.service.simmulatedAnnealing.Categorizer;
import de.uni.osse.ma.service.simmulatedAnnealing.Preprocessor;
import de.uni.osse.ma.service.simmulatedAnnealing.SimmulatedAnnealing;
import de.uni.osse.ma.service.simmulatedAnnealing.Solution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/osse")
@RequiredArgsConstructor
@Slf4j
public class WebService {

    private final Preprocessor preprocessor;
    private final FileInteractionService fileInteractionService;
    private final Categorizer categorizer;

    // TODO: proper naming
    @GetMapping("/greeting")
    // TODO: should this be async? probably
    public Solution anonymity() throws Exception {
        var data = fileInteractionService.readLocalTestData("adult.csv");
        var fieldMetadata = fileInteractionService.readLocalHeaderData("adult_datafields.json");
        final DataWrapper wrapper = new DataWrapper(data, fieldMetadata);
        var processedData = preprocessor.addObfusccations(wrapper);
        fileInteractionService.writeAsCSV(processedData, "adult_processed.csv");

        SimmulatedAnnealing simmulatedAnnealing = new SimmulatedAnnealing(fieldMetadata, "adult_processed.csv", "income_0", categorizer);
        // TODO: run annealing, and return result
        Solution solution = simmulatedAnnealing.calcLocalOptimumSolution();


        if (solution.getScore().compareTo(BigDecimal.ZERO) < 0) {
            // solution is not valid, try again with other parameters maybe?
            log.debug("Couldn't find a solution!");
        }
        // DO STUFF
        return  solution;
    }
}
