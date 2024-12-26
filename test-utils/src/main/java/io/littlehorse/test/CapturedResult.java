package io.littlehorse.test;

import io.littlehorse.test.internal.step.VerifyTaskRunOutputsStep;

/**
 * {@code CapturedResult} is a wrapper for a response. see {@link io.littlehorse.test.internal.step.SearchResultCaptor}
 * @param <T> Search response type
 */
public interface CapturedResult<T> {

    /**
     * Returns the search response value
     */
    T get();

    /**
     * Returns the search response type
     */
    Class<T> type();
}
