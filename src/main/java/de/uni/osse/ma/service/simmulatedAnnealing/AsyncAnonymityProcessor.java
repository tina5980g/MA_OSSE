package de.uni.osse.ma.service.simmulatedAnnealing;

public interface AsyncAnonymityProcessor <T extends ProcessorParams> extends AnonymityProcessor<T> {
    /**
     * @param parameters all parameters necessary for processing
     * @param solutionIdentifier identifier of the solution after it is written to disk. Used to retrieve the solution at a later date
     */
    void processAsync(T parameters, String solutionIdentifier) throws Exception;
}
