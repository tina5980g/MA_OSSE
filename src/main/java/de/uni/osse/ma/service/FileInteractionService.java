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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileInteractionService {
    private final ObjectMapper mapper;
    private final SystemConfiguration systemConfiguration;

    private Path cachedRoot = null;

    public Path getRootPath() {
        try {
            Path datasetDirectory;
            if (StringUtils.isNotBlank(systemConfiguration.getDatasetStorageDir())) {
                datasetDirectory = Path.of(systemConfiguration.getDatasetStorageDir());
            } else {
                datasetDirectory =  Path.of(FileInteractionService.class.getClassLoader().getResource("testData").toURI()).resolve("../../../src/main/resources/testData").toAbsolutePath().normalize();
            }

            if (cachedRoot != null && cachedRoot.equals(datasetDirectory)) {
                return cachedRoot;
            }
            cachedRoot = datasetDirectory;

            datasetDirectory = datasetDirectory.toAbsolutePath();
            if (!Files.exists(datasetDirectory)) {
                throw new IOException("Dataset directory " + datasetDirectory + " does not exist");
            }
            if (!Files.isDirectory(datasetDirectory)) {
                throw new IOException("Dataset directory " + datasetDirectory + " is not a directory");
            }
            if (!Files.isReadable(datasetDirectory)) {
                throw new IOException("Dataset directory " + datasetDirectory + " is not readable");
            }
            if (!Files.isWritable(datasetDirectory)) {
                throw new IOException("Dataset directory " + datasetDirectory + " is not writable");
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

    private Optional<Path> getDatasetDirectory(final String datasetIdentifier, boolean canCreateDir) throws IOException {
        if (datasetIdentifier == null) {
            return Optional.empty();
        }
        try {
            UUID.fromString(datasetIdentifier);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Given datasetIdentifier " + datasetIdentifier + " is not a valid UUID", ex);
        }
        final Path dataDirectory = getRootPath().resolve(datasetIdentifier);
        if (!Files.isDirectory(dataDirectory)) {
            return Optional.empty();
        }
        if (!Files.exists(dataDirectory)) {
            if (canCreateDir) {
                Files.createDirectory(dataDirectory);
            } else {
                return Optional.empty();
            }
        }
        return Optional.of(dataDirectory);
    }

    private ImmutablePair<UUID, Path> getOrCreateDatasetDirectory(final String datasetIdentifier) throws IOException {
        Optional<Path> datasetDirectory = getDatasetDirectory(datasetIdentifier, true);
        if (datasetDirectory.isPresent()) {
            return new ImmutablePair<>(UUID.fromString(datasetIdentifier), datasetDirectory.get());
        }
        UUID newIdentifier = UUID.randomUUID();
        datasetDirectory = getDatasetDirectory(newIdentifier.toString(), true);
        if (datasetDirectory.isPresent()) {
            return new ImmutablePair<>(newIdentifier, datasetDirectory.get());
        }
        throw new IOException("Could not create dataset directory! (Original: " + datasetIdentifier + ", new: " + newIdentifier);
    }

    public Path getPathToFile(final String datasetIdentifier, final FILE_TYPE fileType) throws IOException {
        return getOrCreateDatasetDirectory(datasetIdentifier).getRight().resolve(fileType.getFilename());
    }
    
    public Path getPathToSolution(final String datasetIdentifier, final String solutionIdentifier) throws IOException {
        try {
            UUID.fromString(solutionIdentifier);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Given solutionIdentifier " + solutionIdentifier + " is not a valid UUID", ex);
        }
        return getDatasetDirectory(datasetIdentifier, false)
                .map( path -> path.resolve(solutionIdentifier))
                .orElseThrow(() -> new FileNotFoundException("Solution file " + solutionIdentifier + " for dataset " + datasetIdentifier + " does not exist."));
    }

    public boolean isProcessedFileUpToDate(final String datasetIdentifier) throws IOException {
        Path datasetDirectory = getOrCreateDatasetDirectory(datasetIdentifier).getRight();
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
        Path solutionPath = getPathToSolution(datasetIdentifier, solutionIdentifier);
        if (!Files.exists(solutionPath)) {
            throw new FileNotFoundException("Solution file " + solutionIdentifier + " for dataset " + datasetIdentifier + " does not exist.");
        }
        return Files.readString(solutionPath);
    }

    private InputStreamReader readDatasetFile(final String datasetIdentifier, final FILE_TYPE fileType) throws IOException {
        final Path destinationPath = getDatasetDirectory(datasetIdentifier, false)
                .map(path -> path.resolve(fileType.getFilename()))
                .orElseThrow(() -> new FileNotFoundException("Dataset directory " + datasetIdentifier + " does not exist."));

        return new InputStreamReader(Files.newInputStream(destinationPath, StandardOpenOption.READ));
    }

    public UUID writeDatasetFile(final InputStream multipartFile, final String datasetIdentifier, final FILE_TYPE fileType) throws IOException {
        ImmutablePair<UUID, Path> resultPair = getOrCreateDatasetDirectory(datasetIdentifier);
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
        Path resultPath = getPathToSolution(datasetIdentifier, resultIdentifier);
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
