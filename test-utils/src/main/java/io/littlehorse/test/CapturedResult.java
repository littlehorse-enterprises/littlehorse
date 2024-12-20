package io.littlehorse.test;

import io.littlehorse.test.internal.step.VerifyTaskRunOutputsStep;

/**
 * {@code CapturedResult} is a wrapper for a response. see {@link VerifyTaskRunOutputsStep.SearchResultCaptor}
 * @param <T> Search response type
 */
public interface CapturedResult<T> {

    /**
     * Returns the search response value
     */
    T get();

    Class<T> type();
}
