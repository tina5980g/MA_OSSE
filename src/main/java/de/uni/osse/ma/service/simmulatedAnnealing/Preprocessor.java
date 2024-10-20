package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import de.uni.osse.ma.rs.dto.ColumnType;
import de.uni.osse.ma.rs.dto.DataWrapper;
import de.uni.osse.ma.rs.dto.HeaderInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class Preprocessor {


    // TODO: OutOfMemory: Need to work with stream of rows -> DataWrapper based on an InputStream / CSV-Wrapper
    public String[][] addAdditionalObfuscationLevels(DataWrapper dataWrapper) {
        ArrayList<ArrayList<String>> resultContainer = new ArrayList<>();
        resultContainer.add(new ArrayList<>(dataWrapper.getHeaders().stream().map(HeaderInfo::columnName).toList()));
        resultContainer.addAll(
                dataWrapper.getRows().stream()
                        .map(valueRow -> new ArrayList<>(valueRow.stream()
                                .map(field -> field.representWithObfuscation(0))
                                .toList()))
                        .toList()
        );

        // At this point we have the original data without gradual obfuscation. These colums are added now

        int originalColumnAmount = dataWrapper.getHeaders().size();
        for (int iOriginalColumn = 0; iOriginalColumn < originalColumnAmount; iOriginalColumn++) {

            var columnType = dataWrapper.getHeaders().get(iOriginalColumn).columnType();
            if (columnType == ColumnType.IDENTIFIER || columnType == ColumnType.UNDEFINED) {
                continue;
            }
            // TODO: there needs to be a better way then catching the exception
            for (int j = 1; true; j++) {
                try {
                    for (int iRow = 1; iRow < dataWrapper.getRows().size() + 1; iRow++) {
                        resultContainer.get(iRow).add(dataWrapper.getRows().get(iRow-1).get(iOriginalColumn).representWithObfuscation(j));
                    }
                    resultContainer.getFirst().add(dataWrapper.getHeaders().get(iOriginalColumn).columnName() + "_" + j);
                } catch (NoMoreAnonymizationLevelsException _) {
                    break;
                }
            }
        }

        var result = new String[resultContainer.size()][resultContainer.getFirst().size()];
        for (int i = 0; i < resultContainer.size(); i++) {
            for (int j = 0; j < resultContainer.getFirst().size(); j++) {
                result[i][j] = resultContainer.get(i).get(j);
            }
        }
        return result;
    }
}
