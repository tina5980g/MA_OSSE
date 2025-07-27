package de.uni.osse.ma.service.simmulatedAnnealing;


import de.uni.osse.ma.service.FileInteractionService;
import lombok.Builder;
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

@Component
@Slf4j
/** Trains a categorizing model and calculates it's accuracy */
// TODO: Can this safely run in parallel?
public class Categorizer implements InitializingBean {
    private static boolean READY = false;

    @Override
    public void afterPropertiesSet() {
        if (!READY) {
            initialize();
        }
    }

    public static void initialize(){
        try {
            READY = initializeViaSyscall();
        } catch (Exception e) {
            throw new BeanInitializationException("Catboost initialization encountered an error!", e);
        }
        if (!READY) throw new BeanInitializationException("Catboost initialization not successful!");
    }

    public BigDecimal scoreModelAccurary(final ClassificationScriptArguments.ClassificationScriptArgumentsBuilder scriptArguments) throws Exception {
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
                // TODO: proper exception
                throw new Exception("Scoring result for '" + args.threadId() + "' has invalid format.");
            }
            String score = lastResult.split(";")[1];
            log.debug("{} scored {}", String.join(", ", args.solutionColumns()), score);
            if (score.equals("-inf")) {
                return BigDecimal.ONE.negate();
            }
            return new BigDecimal(score);
        }
    }

    private static boolean initializeViaSyscall() throws IOException, URISyntaxException {
        int exitCode = callPythonScript("init_categorizer.py");
        return exitCode == 0;
    }

    private static String resolvePythonScriptPath(String filename) throws URISyntaxException, FileNotFoundException {
        URL resourceURL = Categorizer.class.getClassLoader().getResource("python/" + filename);
        if (resourceURL == null) {
            throw new FileNotFoundException("Failed to load python script '" + filename + "'");
        }
        return Path.of(resourceURL.toURI()).toAbsolutePath().toString();
    }

    private static int callPythonScript(String filename, String ...args) throws IOException, URISyntaxException {
        // TODO: python runtime configurable python/python3 or something different
        final StringBuilder line = new StringBuilder("python " + resolvePythonScriptPath(filename));
        for (String arg : args) {
            line.append(' ').append(arg);
        }

        CommandLine cmdLine = CommandLine.parse(line.toString());
        // TODO: logging to file? SLF4J does not handle OutputStreams
        PumpStreamHandler streamHandler = new PumpStreamHandler(System.out);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);

        return executor.execute(cmdLine);
    }

    @Builder
    public record ClassificationScriptArguments(String datasetFilename, List<String> solutionColumns, int equivalenceclassSize, double maxSuppression, String targetColumn, String threadId) {
        private static final Path ROOT_PATH = FileInteractionService.getRootPath();

        public Path getResultFile() {
            return ROOT_PATH.resolve(threadId());
        }

        private String[] toArgs() {
            String[] args = new String[7];
            args[0] = "-f " + ROOT_PATH.resolve(datasetFilename);
            args[1] = "-cs " + String.join(" ", solutionColumns);
            args[2] = "-k " + equivalenceclassSize;
            args[3] = "-mS " + maxSuppression;
            args[4] = "-ct " + targetColumn;
            args[5] = "-op " + ROOT_PATH;
            args[6] = "-oid " + threadId;

            return args;
        }
    }
}
