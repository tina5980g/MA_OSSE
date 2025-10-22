package de.uni.osse.ma.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/osse")
@RequiredArgsConstructor
@Slf4j
public class WebService {

    private final Preprocessor preprocessor;
    private final FileInteractionService fileInteractionService;
    private final SimulatedAnnealing simulatedAnnealing;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/upload/dataset", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponseDto uploadDataset(@RequestParam(name = "data") MultipartFile dataFile,
                                           @RequestParam(name = "dataIdentifier", required = false) String dataIdentifier) {

        UUID identifier;

        try (InputStream stream = dataFile.getInputStream()) {
            identifier = fileInteractionService.writeDatasetFile(stream, dataIdentifier, FILE_TYPE.DATA_SET);
        } catch (IOException e) {
            log.error("Could not read or store data file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read or store data file.", e);
        }

        return new UploadResponseDto(identifier, !Objects.equals(identifier.toString(), dataIdentifier));
    }

    @PostMapping(value = "/upload/headers", consumes = MediaType.APPLICATION_JSON_VALUE)
    public UploadResponseDto uploadHeaders(@RequestBody HeadersDto headerFile,
                                           @RequestParam(name = "dataIdentifier", required = false) String dataIdentifier) {
        UUID identifier;

        try (InputStream stream = new ByteArrayInputStream(objectMapper.writeValueAsString(headerFile).getBytes(StandardCharsets.UTF_8))) {
            identifier = fileInteractionService.writeDatasetFile(stream, dataIdentifier, FILE_TYPE.HEADER_DATA);
        } catch (IOException e) {
            log.error("Could not read or store data file", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read or store header file.", e);
        }

        return new UploadResponseDto(identifier, !Objects.equals(identifier.toString(), dataIdentifier));
    }


    @GetMapping("/strategy/{dataIdentifier}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    // TODO: identify number of classes and include in response with warning on higher amount "You requested a strategy for x target classes. This will take a while and may not give a good result!"
    public String getStrategy(@PathVariable("dataIdentifier") String dataIdentifier,
                              @RequestParam(name = "k", required = false) Integer kLevel,
                              @RequestParam(name = "maxSuppression", required = false) String maxSuppression,
                              @RequestParam(name = "featureColumns", required = false) List<String> featureColumns,
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

        // Dirty, but works
        fieldMetadata = new HeadersDto(fieldMetadata.columns().stream().map(headerInfo -> {
            if (CollectionUtils.isNotEmpty(featureColumns) && featureColumns.contains(headerInfo.columnName())) {
                return headerInfo;
            }
            if (CollectionUtils.isEmpty(featureColumns) && !targetColumn.contains(headerInfo.columnName())) {
                return headerInfo;
            }
            return new HeaderInfo(headerInfo.columnName(), headerInfo.columnIdentifier(), new ObfuscationInfo(ObfuscationInfo.ObfuscationStrategy.STATIC, DataType.IGNORE, null));
        }).toList());

        final String solutionIdentifier = UUID.randomUUID().toString();
        fileInteractionService.writeSolution(dataIdentifier, solutionIdentifier, null);
        BigDecimal maxSuppressionValue = null;
        if (maxSuppression != null) {
            try {
                maxSuppressionValue = new BigDecimal(maxSuppression);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid max suppression value: " + maxSuppression);
            }
        }
        SimulatedAnnealing.Parameters.builder()
                .dataIdentifier(dataIdentifier)
                .headerSource(fieldMetadata)
                .classificationTarget(targetColumn)
                .solutionIdentifier(solutionIdentifier)
                .kLevel(kLevel)
                .maxSuppression(maxSuppressionValue)
                .build();

        simulatedAnnealing.calcLocalOptimumSolution(SimulatedAnnealing.Parameters.builder()
                .dataIdentifier(dataIdentifier)
                .headerSource(fieldMetadata)
                .classificationTarget(targetColumn)
                .solutionIdentifier(solutionIdentifier)
                .kLevel(kLevel)
                .maxSuppression(maxSuppressionValue)
                .build());
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
