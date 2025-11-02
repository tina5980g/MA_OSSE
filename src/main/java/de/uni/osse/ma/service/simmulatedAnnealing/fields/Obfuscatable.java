package de.uni.osse.ma.service.simmulatedAnnealing.fields;

import de.uni.osse.ma.exceptions.NoMoreAnonymizationLevelsException;
import jakarta.annotation.Nullable;

import java.util.Map;

public interface Obfuscatable {

    String representWithObfuscation(int level, @Nullable Map<String, Object> params) throws NoMoreAnonymizationLevelsException;

}
