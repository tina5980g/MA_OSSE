package de.uni.osse.ma.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import de.uni.osse.ma.rs.dto.HeadersDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
// TODO: all those paths need to be done better, ideally configurable
public class FileInteractionService {
    private final ObjectMapper mapper;

    public List<String[]> readLocalTestData(String filename) throws IOException, CsvException {
        try(var inputStream = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("testData/"+ filename))) {
            try (CSVReader csvReader = new CSVReader(inputStream)) {
                return csvReader.readAll();
            }
        }
    }

    public HeadersDto readLocalHeaderData(String filename) throws IOException {
        try(var parser = mapper.createParser(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("testData/"+ filename)))) {
            return parser.readValueAs(HeadersDto.class);
        }
    }


    public void writeAsCSV(final Stream<String[]> data, String filename) throws IOException {
        Path path;
        try {
            path = Path.of(this.getClass().getClassLoader().getResource("testData").toURI()).resolve("../../../src/main/resources/testData").resolve(filename).normalize();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        if (!Files.exists(path)) {
            // TODO: works on different OS?
            Files.createFile(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-rw----"))); // owner and group can read and write
        }

        try(CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            data.sequential().forEach(csvWriter::writeNext);
        }
    }
}
