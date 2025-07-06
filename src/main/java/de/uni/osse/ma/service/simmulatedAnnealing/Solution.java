package de.uni.osse.ma.service.simmulatedAnnealing;

import de.uni.osse.ma.rs.dto.HeaderInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.SerializationUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;


@Getter
public class Solution {
    /**
     * each header and it's anonymity level
     * -- GETTER --
     * return modifiable Map
     */
    private final HashMap<HeaderInfo, Integer> anonymityLevels;
    @Setter
    private BigDecimal score;

    public Solution(HashMap<HeaderInfo, Integer> anonymityLevels) {
        // deep copy
        this.anonymityLevels = SerializationUtils.clone(anonymityLevels);
        score = BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return Objects.equals(anonymityLevels, solution.anonymityLevels) && Objects.equals(getScore(), solution.getScore());
    }

    @Override
    public int hashCode() {
        return Objects.hash(anonymityLevels, getScore());
    }
}
