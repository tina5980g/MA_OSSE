package de.uni.osse.ma.rs;

import de.uni.osse.ma.service.FileInteractionService;
import de.uni.osse.ma.service.simmulatedAnnealing.Preprocessor;
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


    @GetMapping("/greeting")
    public String anonymity() throws Exception {
        var data = fileInteractionService.readLocalTestData("adult.csv", "csv");
        var fieldMetadata = fileInteractionService.readLocalTestData("adult_datafields.json", "json");
        var processedData = preprocessor.addAdditionalObfuscationLevels(data);
        fileInteractionService.writeAsCSV(processedData);


        // DO STUFF
        return  "Hello World";
    }
}
