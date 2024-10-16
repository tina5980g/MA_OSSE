package de.uni.osse.ma.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import de.uni.osse.ma.rs.dto.DataWrapper;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class FileInteractionService {

    public DataWrapper readLocalTestData(String filename, String filetype) throws IOException, CsvException {
        try(var inputStream = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("testData/"+ filename))) {
            if (filetype.equalsIgnoreCase("csv")) {
                try (CSVReader csvReader = new CSVReader(inputStream)) {
                    String[] headers = csvReader.readNext();
                    return new DataWrapper(headers, csvReader.readAll());
                }
            }
        }

        return null;

    }

    public void writeAsCSV(final String[][] data) throws IOException, URISyntaxException {
        var path = Paths.get("/Users/tinayau/IdeaProjects/MA_OSSE/src/main/resources/testData").resolve("processData.csv");

        try(CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(path))) {
            for (String[] datum : data) {
                csvWriter.writeNext(datum);
            }
        }
    }
}
