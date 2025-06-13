package de.uni.osse.ma.rs;

import de.uni.osse.ma.rs.dto.DataWrapper;
import de.uni.osse.ma.service.FileInteractionService;
import de.uni.osse.ma.service.simmulatedAnnealing.Categorizer;
import de.uni.osse.ma.service.simmulatedAnnealing.Preprocessor;
import de.uni.osse.ma.service.simmulatedAnnealing.SimmulatedAnnealing;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/osse")
@RequiredArgsConstructor
public class WebService {

    private final Preprocessor preprocessor;
    private final FileInteractionService fileInteractionService;
    private final Categorizer categorizer;

    // TODO: proper naming
    @GetMapping("/greeting")
    public String anonymity() throws Exception {
        var data = fileInteractionService.readLocalTestData("adult.csv");
        var fieldMetadata = fileInteractionService.readLocalHeaderData("adult_datafields.json");
        final DataWrapper wrapper = new DataWrapper(data, fieldMetadata);
        var processedData = preprocessor.addObfusccations(wrapper);
        fileInteractionService.writeAsCSV(processedData, "adult_processed.csv");

        SimmulatedAnnealing simmulatedAnnealing = new SimmulatedAnnealing(fieldMetadata, "adult_processed.csv", "income_0", categorizer);
        // TODO: run annealing, and return result

        // TODO: should this be async? probably not

        // DO STUFF
        return  "Hello World";
    }
}
