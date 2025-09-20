package de.uni.osse.ma.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import de.uni.osse.ma.config.SystemConfiguration;
import de.uni.osse.ma.rs.dto.HeadersDto;
import de.uni.osse.ma.service.simmulatedAnnealing.FILE_TYPE;
import de.uni.osse.ma.service.simmulatedAnnealing.Solution;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileInteractionService {
    private final ObjectMapper mapper;
    private final SystemConfiguration systemConfiguration;

    public Path getRootPath() {
        try {
            Path datasetDirectory;
            if (StringUtils.isNotBlank(systemConfiguration.getDatasetStorageDir())) {
                datasetDirectory = Path.of(systemConfiguration.getDatasetStorageDir());
            } else {
                datasetDirectory =  Path.of(FileInteractionService.class.getClassLoader().getResource("testData").toURI()).resolve("../../../src/main/resources/testData").toAbsolutePath().normalize();
            }
            if (!Files.exists(datasetDirectory)) {
                throw new IOException("Dataset directory " + datasetDirectory.toAbsolutePath() + " does not exist");
            }
            if (!Files.isDirectory(datasetDirectory)) {
                throw new IOException("Dataset directory " + datasetDirectory.toAbsolutePath() + " is not a directory");
            }
            if (!Files.isReadable(datasetDirectory)) {
                throw new IOException("Dataset directory " + datasetDirectory.toAbsolutePath() + " is not readable");
            }
            if (!Files.isWritable(datasetDirectory)) {
                throw new IOException("Dataset directory " + datasetDirectory.toAbsolutePath() + " is not writable");
            }
            log.info("Using DatasetDirectory {}", datasetDirectory);
            return datasetDirectory;
        } catch (Throwable e) {
            log.error("Unrecoverable configuration error. Can't access datasets!", e);
            // No way to recover from there
            System.exit(3);
            return null;
        }
    }

    private ImmutablePair<UUID, Path> getDatasetDirectory(final String datasetIdentifier) throws IOException {
        UUID usedIdentifier;
        if (datasetIdentifier == null || !getRootPath().resolve(datasetIdentifier).toFile().isDirectory()) {
            usedIdentifier = UUID.randomUUID();
        } else {
            usedIdentifier = UUID.fromString(datasetIdentifier);
        }

        final Path dataDirectory = getRootPath().resolve(usedIdentifier.toString());

        if (!dataDirectory.toFile().isDirectory()) {
            Files.createDirectories(dataDirectory);
        }
        return new ImmutablePair<>(usedIdentifier, dataDirectory);
    }

    public Path getPathToFile(final String datasetIdentifier, final FILE_TYPE fileType) throws IOException {
        return getDatasetDirectory(datasetIdentifier).getRight().resolve(fileType.getFilename());
    }
    
    public Path getPathToSolution(final String datasetIdentifier, final String solutionIdentifier) throws IOException {
        return getDatasetDirectory(datasetIdentifier).getRight().resolve(solutionIdentifier);
    }

    public boolean isProcessedFileUpToDate(final String datasetIdentifier) throws IOException {
        Path datasetDirectory = getDatasetDirectory(datasetIdentifier).getRight();
        if (!Files.exists(datasetDirectory.resolve(FILE_TYPE.PROCESSED_DATA_SET.getFilename()))) {
            return false;
        }
        FileTime processedTime = Files.getLastModifiedTime(datasetDirectory.resolve(FILE_TYPE.PROCESSED_DATA_SET.getFilename()));
        FileTime dataTime = Files.getLastModifiedTime(datasetDirectory.resolve(FILE_TYPE.DATA_SET.getFilename()));
        FileTime headerTime = Files.getLastModifiedTime(datasetDirectory.resolve(FILE_TYPE.HEADER_DATA.getFilename()));

        return processedTime.compareTo(dataTime) > 0 && headerTime.compareTo(dataTime) > 0;
    }

    public List<String[]> readStoredDatasetValue(String datasetIdentifier) throws IOException, CsvException {
        try(var inputStream = readDatasetFile(datasetIdentifier, FILE_TYPE.DATA_SET)) {
            try (CSVReader csvReader = new CSVReader(inputStream)) {
                return csvReader.readAll();
            }
        }
    }

    public HeadersDto readStoredHeaderDataValue(String datasetIdentifier) throws IOException {
        try(var parser = mapper.createParser(readDatasetFile(datasetIdentifier, FILE_TYPE.HEADER_DATA))) {
            return parser.readValueAs(HeadersDto.class);
        }
    }
    
    public String readStoredSolution(String datasetIdentifier, String solutionIdentifier) throws IOException {
        File solutionFile = getPathToSolution(datasetIdentifier, solutionIdentifier).toFile();
        if (!solutionFile.exists()) {
            throw new FileNotFoundException("Solution file " + solutionIdentifier + " for dataset " + datasetIdentifier + " does not exist.");
        }
        return Files.readString(solutionFile.toPath());
    }

    private InputStreamReader readDatasetFile(final String datasetIdentifier, final FILE_TYPE fileType) throws IOException {
        ImmutablePair<UUID, Path> resultPair = getDatasetDirectory(datasetIdentifier);
        Path destinationPath = resultPair.getRight().resolve(fileType.getFilename());

        return new InputStreamReader(Files.newInputStream(destinationPath, StandardOpenOption.READ));
    }

    public UUID writeDatasetFile(final InputStream multipartFile, final String datasetIdentifier, final FILE_TYPE fileType) throws IOException {
        ImmutablePair<UUID, Path> resultPair = getDatasetDirectory(datasetIdentifier);
        Path destinationPath = resultPair.getRight().resolve(fileType.getFilename());
        if (!Files.exists(destinationPath)) {
            Files.createFile(destinationPath);
            //Files.setPosixFilePermissions(destinationPath, READ_WRITE_POSIX);
        }

        try (InputStream inputStream = multipartFile) {
            Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return resultPair.getLeft();
    }

    public void writeProcessedCSV(final Stream<String[]> data, String datasetIdentifier) throws IOException {
        Path destinationPath = getPathToFile(datasetIdentifier, FILE_TYPE.PROCESSED_DATA_SET);
        if (!Files.exists(destinationPath)) {
            Files.createFile(destinationPath);
        }

        try(CSVWriter csvWriter = new CSVWriter(Files.newBufferedWriter(destinationPath, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            data.sequential().forEach(csvWriter::writeNext);
        }
    }

    public void writeSolution(String datasetIdentifier, String resultIdentifier, Object allTimeBestSolution) throws IOException {
        Path resultPath = getRootPath().resolve(datasetIdentifier).resolve(resultIdentifier);
        if (allTimeBestSolution == null) {
            Files.deleteIfExists(resultPath);
            Files.createFile(resultPath);
            // empty file marks a solution that has been requested, but not yet found
            return;
        }
        ObjectWriter objectWriter = mapper.writerFor(Solution.class);
        objectWriter.writeValue(resultPath.toFile(), allTimeBestSolution);
    }
}
