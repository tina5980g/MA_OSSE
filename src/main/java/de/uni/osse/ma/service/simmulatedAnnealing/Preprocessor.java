package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.rs.dto.DataWrapper;
import de.uni.osse.ma.rs.dto.HeaderInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class Preprocessor {


    public Stream<String[]> addObfusccations(DataWrapper dataWrapper) {
        // We have to retrieve elements in high frequency, array should be quicker here
        HeaderInfo[] headers = dataWrapper.getHeaders().toArray(new HeaderInfo[0]);

        // at least headers.length
        final List<String> csvHeaders = new ArrayList<>(headers.length * 2);

        for (int i = 0; i < dataWrapper.getHeaders().size(); i++) {
            final HeaderInfo curHeader = headers[i];
            for (int j = 0; j <= curHeader.dataType().getMaxObfuscation(); j++) {
                csvHeaders.add(curHeader.columnName() + "_" + j);
            }
        }

        // Stream.of defaults to vararg
        Stream<String[]> csvFirstRow1 = Stream.ofNullable(csvHeaders.toArray(new String[0]));

        Stream<String[]> dataRows = dataWrapper.getRows().stream()
                .map(dataFields -> {
                    List<String> results = new ArrayList<>();
                    for (int fieldIndex = 0; fieldIndex < dataFields.size(); fieldIndex++) {
                        final HeaderInfo curHeader = headers[fieldIndex];
                        for (int levelIndex = 0; levelIndex <= curHeader.dataType().getMaxObfuscation(); levelIndex++) {
                            results.add(dataFields.get(fieldIndex).representWithObfuscation(levelIndex));
                        }
                    }
                    return results.toArray(String[]::new);
                });
        return Stream.concat(csvFirstRow1, dataRows);
    }
}
