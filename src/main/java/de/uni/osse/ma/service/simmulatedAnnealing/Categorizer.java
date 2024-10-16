package de.uni.osse.ma.service.simmulatedAnnealing;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

@Component
@Slf4j
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

    public void trainModel() throws IOException, URISyntaxException {
        callPythonScript("calc_classification_score.py");
    }

    private static int callPythonScript(String filename, String ...args) throws IOException, URISyntaxException {
        // TODO: Multithreaded
        final StringBuilder line = new StringBuilder("python3 " + resolvePythonScriptPath(filename));
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
}
