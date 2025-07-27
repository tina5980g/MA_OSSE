package de.uni.osse.ma.rs;

import de.uni.osse.ma.rs.dto.*;
import de.uni.osse.ma.service.FileInteractionService;
import de.uni.osse.ma.service.simmulatedAnnealing.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/osse")
@RequiredArgsConstructor
@Slf4j
public class WebService {

    private final Preprocessor preprocessor;
    private final FileInteractionService fileInteractionService;
    private final SimulatedAnnealing simulatedAnnealing;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponseDto uploadDataset(
            @RequestParam(name = "data", required = false) MultipartFile dataFile,
                                           @RequestParam(name = "headers", required = false) MultipartFile headerFile,
                                           @RequestParam(name = "dataIdentifier", required = false) String dataIdentifier) {
        if ((dataFile == null || dataFile.isEmpty()) && (headerFile == null || headerFile.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required parameters. At least one of 'data' or 'header' file need to exist");
        }

        UUID identifier = null;

        if (dataFile != null && !dataFile.isEmpty()) {
            try (InputStream stream = dataFile.getInputStream()){
                identifier = fileInteractionService.writeDatasetFile(stream, dataIdentifier, FILE_TYPE.DATA_SET);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read or store data file.", e);
            }
        }

        if (headerFile != null && !headerFile.isEmpty()) {
            try (InputStream stream = headerFile.getInputStream()){
                identifier = fileInteractionService.writeDatasetFile(stream, identifier == null ? dataIdentifier : identifier.toString(), FILE_TYPE.HEADER_DATA);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read or store header file.", e);
            }
        }

        return new UploadResponseDto(identifier);
    }

    @GetMapping("/strategy/{dataIdentifier}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String getStrategy(@PathVariable("dataIdentifier") String dataIdentifier,
                                                @RequestParam(name="featureColumns", required = false) List<String> featureColumns,
                                                @RequestParam(name = "targetColumn") String targetColumn) throws Exception {
        var data = fileInteractionService.readStoredDatasetValue(dataIdentifier);
        var fieldMetadata = fileInteractionService.readStoredHeaderDataValue(dataIdentifier);

        // preprocess only if either data or header are younger than an already existing processed file
        if (!fileInteractionService.isProcessedFileUpToDate(dataIdentifier)) {
            log.info("Processing file for {}.", dataIdentifier);
            final DataWrapper wrapper = new DataWrapper(data, fieldMetadata);
            var processedData = preprocessor.addObfusccations(wrapper);
            fileInteractionService.writeProcessedCSV(processedData, dataIdentifier);
        }        
        
        if (CollectionUtils.isNotEmpty(featureColumns)) {
            // Dirty, but works
            fieldMetadata = new HeadersDto(fieldMetadata.columns().stream().map(headerInfo -> {
                if (targetColumn.contains(headerInfo.columnName())) {
                    return headerInfo;
                }
                if (featureColumns.contains(headerInfo.columnName())) {
                    return headerInfo;
                }
                return new HeaderInfo(headerInfo.columnName(), headerInfo.columnIdentifier(), DataType.IGNORE, ColumnType.PSEUDO_IDENTIFIER);
            }).toList());
        }

        final String solutionIdentifier = UUID.randomUUID().toString();
        fileInteractionService.writeSolution(dataIdentifier, solutionIdentifier, null);
        simulatedAnnealing.calcLocalOptimumSolution(dataIdentifier, fieldMetadata, targetColumn, solutionIdentifier);
        
        return solutionIdentifier;
    }
    
    // String representation of a Solution (Solution contains a map, so Jackson gets confused when deserializing
    @GetMapping("/strategy/{dataIdentifier}/{solutionIdentifier}")
    public String getSolution(@PathVariable("dataIdentifier") String dataIdentifier, @PathVariable("solutionIdentifier") String solutionIdentifier) throws Exception {
        String solution = fileInteractionService.readStoredSolution(dataIdentifier, solutionIdentifier);
        if (solution == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Solution not yet found. Try again later.");
        }
        return solution;
    }
}
