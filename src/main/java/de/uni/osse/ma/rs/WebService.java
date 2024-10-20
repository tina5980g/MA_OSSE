package de.uni.osse.ma.rs;

import de.uni.osse.ma.rs.dto.DataWrapper;
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
        var data = fileInteractionService.readLocalTestData("adult.csv");
        var fieldMetadata = fileInteractionService.readLocalHeaderData("adult_datafields.json");
        final DataWrapper wrapper = new DataWrapper(data, fieldMetadata);
        var processedData = preprocessor.addAdditionalObfuscationLevels(wrapper);
        fileInteractionService.writeAsCSV(processedData, "adult_processed.csv");


        // DO STUFF
        return  "Hello World";
    }
}
