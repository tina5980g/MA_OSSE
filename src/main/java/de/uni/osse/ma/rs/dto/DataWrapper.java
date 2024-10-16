package de.uni.osse.ma.rs.dto;

import de.uni.osse.ma.service.simmulatedAnnealing.fields.DataField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.DateField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.IdentifyingField;
import de.uni.osse.ma.service.simmulatedAnnealing.fields.NonIdentifyingField;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DataWrapper {
    // TODO: use custom class instead of ImmutablePair
    private final List<ImmutablePair<String, Class<? extends DataField<?>>>> headers;
    private final List<List<DataField<?>>> rows;

    public DataWrapper(String[] headers, List<String[]> rawValues) {
        this.headers = List.of(
                new ImmutablePair<>(headers[0], IdentifyingField.class),
                new ImmutablePair<>(headers[1], DateField.class),
                new ImmutablePair<>(headers[2], NonIdentifyingField.class)
                );

        this.rows = rawValues.stream().map(rawValueRow -> {
            List<DataField<?>> parsedRow = new ArrayList<>(rawValueRow.length);
            parsedRow.add(new IdentifyingField(rawValueRow[0]));
            parsedRow.add(new DateField(rawValueRow[1]));
            parsedRow.add(new NonIdentifyingField(rawValueRow[2]));

            return parsedRow;
        }).toList();

        for (int i = 0; i < this.headers.size(); i++) {
            sanityCheck(i);
        }

    }

    private void sanityCheck(int columnIndex) {
        var expectedClass = headers.get(columnIndex).getRight();
        if(!rows.stream().allMatch(row -> row.get(columnIndex).getClass().equals(expectedClass))) {
            throw new RuntimeException("Rows do not match");
        }
    }
}
