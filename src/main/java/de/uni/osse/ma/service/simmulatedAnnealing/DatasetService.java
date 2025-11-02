package de.uni.osse.ma.service.simmulatedAnnealing;

import com.opencsv.exceptions.CsvException;
import de.uni.osse.ma.rs.dto.DataWrapper;
import de.uni.osse.ma.service.FileInteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatasetService {

    private final FileInteractionService fileInteractionService;
    private final Preprocessor preprocessor;

    public boolean updateProcessedFile(final String dataIdentifier) throws IOException, CsvException {
        var data = fileInteractionService.readStoredDatasetValue(dataIdentifier);
        var fieldMetadata = fileInteractionService.readStoredHeaderDataValue(dataIdentifier);

        // preprocess only if either data or header are younger than an already existing processed file
        if (!fileInteractionService.isProcessedFileUpToDate(dataIdentifier)) {
            log.info("Processing file for {}.", dataIdentifier);
            final DataWrapper wrapper = new DataWrapper(data, fieldMetadata);
            log.info("Starting preprocessing for {}.", dataIdentifier);
            var processedData = preprocessor.addObfusccations(wrapper);
            fileInteractionService.writeProcessedCSV(processedData, dataIdentifier);
            return true;
        }
        return false;
    }
}
