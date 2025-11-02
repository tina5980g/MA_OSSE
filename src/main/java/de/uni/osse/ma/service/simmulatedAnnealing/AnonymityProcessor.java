package de.uni.osse.ma.service.simmulatedAnnealing;

public interface AnonymityProcessor<T extends ProcessorParams> {

    Solution process(T additionalParams) throws Exception;
    AnonymizationAlgorithm getAlgorithm();
}
