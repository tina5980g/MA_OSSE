package de.uni.osse.ma.service;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.input.ReaderInputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DownloadPreparationService {
    private final FileInteractionService fileInteractionService;


    public ResponseEntity<InputStreamResource> prepareSolutionForDownload(String dataIdentifier, String solution) throws IOException {
        Stream<String[]> stream = fileInteractionService.applySolution(dataIdentifier, solution);



        File tempFile = createTempFile();
        // write to tempFile, then offer tempFile for download
        try (final FileWriter fileWriter = new FileWriter(tempFile)) {
            try (CSVWriter writer = new CSVWriter(fileWriter)) {
                stream.forEach(writer::writeNext);
            }
        }

        return ResponseEntity.ok()
                .contentType(new MediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"anonymized.csv\"")
                .body(new InputStreamResource(ReaderInputStream.builder().setFile(tempFile).get()));
    }

    private File createTempFile() throws IOException {
        return Files.createTempFile(UUID.randomUUID().toString(), ".csv").toFile();
    }

}
