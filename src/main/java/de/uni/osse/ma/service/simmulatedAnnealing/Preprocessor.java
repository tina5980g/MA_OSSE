package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.rs.dto.DataWrapper;
import de.uni.osse.ma.rs.dto.HeaderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class Preprocessor {

    public Stream<String[]> addObfusccations(DataWrapper dataWrapper) {
        // We have to retrieve elements in high frequency, array should be quicker here
        HeaderInfo[] headers = dataWrapper.getHeaders().toArray(new HeaderInfo[0]);

        // at least headers.length
        final List<String> csvHeaders = new ArrayList<>(headers.length * 2);

        for (int i = 0; i < dataWrapper.getHeaders().size(); i++) {
            final HeaderInfo curHeader = headers[i];
            switch (curHeader.obfuscationInfo().strategy()) {
                case STATIC -> {
                    try {
                        for (int j = 0; j <= dataWrapper.maxObfuscationFor(curHeader); j++) {
                            csvHeaders.add(curHeader.columnName() + "_" + j);
                        }
                    } catch (NoMoreAnonymizationLevelsException _) {
                    }

                }
                case PROVIDED -> csvHeaders.add(curHeader.columnName() + "_" + curHeader.obfuscationInfo().level());
            }
        }

        Stream<String[]> dataRows = dataWrapper.getRows().stream()
                .map(dataFields -> {
                    List<String> results = new ArrayList<>();
                    for (int fieldIndex = 0; fieldIndex < dataFields.size(); fieldIndex++) {
                        final HeaderInfo curHeader = headers[fieldIndex];
                        switch (curHeader.obfuscationInfo().strategy()) {
                            case STATIC -> {
                                try {
                                    for (int levelIndex = 0; levelIndex <= dataWrapper.maxObfuscationFor(curHeader); levelIndex++) {
                                        try {
                                            results.add(dataFields.get(fieldIndex).representWithObfuscation(levelIndex, curHeader.obfuscationInfo().params()));
                                        } catch (Exception e) {
                                            log.error("error during preprocessing of {}", fieldIndex, e);
                                            throw e;
                                        }
                                    }
                                } catch (NoMoreAnonymizationLevelsException _) {

                                }
                            }
                            // caller provides the levels, so we don't need to add additional columns for different levels.
                            case PROVIDED ->
                                    results.add(dataFields.get(fieldIndex).representWithObfuscation(0, Collections.emptyMap()));
                        }

                    }
                    return results.toArray(String[]::new);
                });
        return Stream.concat(Stream.ofNullable(csvHeaders.toArray(new String[0])), dataRows);
    }
}
