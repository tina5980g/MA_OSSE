package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.config.SystemConfiguration;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/** Trains a categorizing model and calculates it's accuracy */
@Component
@Slf4j
@RequiredArgsConstructor
public class Categorizer implements InitializingBean {
    private static boolean READY = false;

    private final SystemConfiguration systemConfiguration;

    @Override
    public void afterPropertiesSet() {
        if (!READY) {
            initialize();
        }
    }

    public void initialize(){
        try {
            READY = initializeViaSyscall();
        } catch (Exception e) {
            throw new BeanInitializationException("Catboost initialization encountered an error!", e);
        }
        if (!READY) throw new BeanInitializationException("Catboost initialization not successful!");
    }

    public BigDecimal scoreModelAccurary(final ClassificationScriptArguments.ClassificationScriptArgumentsBuilder scriptArguments) throws IOException, URISyntaxException {
        scriptArguments.threadId(Thread.currentThread().getName());
        var args = scriptArguments.build();
        int exitCode = callPythonScript("calc_classification_score.py", args.toArgs());
        if (exitCode != 0) {
            return BigDecimal.ONE.negate();
        }
        // python writes the score to a file from which we read the score
        Path resultPath = args.getResultFile();
        ReversedLinesFileReader.Builder builder = ReversedLinesFileReader.builder()
                .setPath(resultPath)
                .setCharset(StandardCharsets.UTF_8);
        try (ReversedLinesFileReader reverseReader = builder.get()) {
            String lastResult = reverseReader.readLine();
            if (lastResult.split(";").length != 4) {
                log.error("Invalid format: {}", lastResult);
                throw new IOException("Scoring result for '" + args.threadId() + "' has invalid format.");
            }
            String score = lastResult.split(";")[1];
            log.debug("{} scored {}", String.join(", ", args.solutionColumns()), score);
            if (score.equals("-inf")) {
                return BigDecimal.ONE.negate();
            }
            return new BigDecimal(score);
        }
    }

    private boolean initializeViaSyscall() throws IOException, URISyntaxException {
        int exitCode = callPythonScript("init_categorizer.py");
        return exitCode == 0;
    }

    private String resolvePythonScriptPath(String filename) throws URISyntaxException, FileNotFoundException {
        URL resourceURL = Categorizer.class.getClassLoader().getResource("python/" + filename);
        if (resourceURL == null) {
            throw new FileNotFoundException("Failed to load python script '" + filename + "'");
        }
        return Path.of(resourceURL.toURI()).toAbsolutePath().toString();
    }

    private int callPythonScript(String filename, String ...args) throws IOException, URISyntaxException {
        final StringBuilder line = new StringBuilder(systemConfiguration.getPythonExecutable() + " " + resolvePythonScriptPath(filename));
        for (String arg : args) {
            line.append(' ').append(arg);
        }

        CommandLine cmdLine = CommandLine.parse(line.toString());
        // TODO: logging to file instead? SLF4J does not handle OutputStreams
        // python logs are written to System.out
        PumpStreamHandler streamHandler = new PumpStreamHandler(System.out);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);
        log.debug("calling script with {}", cmdLine);

        return executor.execute(cmdLine);
    }

    @Builder
    public record ClassificationScriptArguments(String datasetFilename, List<String> solutionColumns, int equivalenceclassSize, BigDecimal maxSuppression, String targetColumn, String threadId, Path rootPath) {

        public Path getResultFile() {
            return rootPath.resolve(threadId());
        }

        private String[] toArgs() {
            String[] args = new String[7];
            args[0] = "-f " + rootPath.resolve(datasetFilename);
            args[1] = "-cs " + String.join(" ", solutionColumns);
            args[2] = "-k " + equivalenceclassSize;
            args[3] = "-mS " + maxSuppression;
            args[4] = "-ct " + targetColumn;
            args[5] = "-op " + rootPath;
            args[6] = "-oid " + threadId;

            return args;
        }
    }
}
