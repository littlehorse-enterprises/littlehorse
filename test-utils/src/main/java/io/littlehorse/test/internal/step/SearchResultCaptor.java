package io.littlehorse.test.internal.step;

import com.google.protobuf.Message;
import io.littlehorse.test.CapturedResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code SearchResultCaptor} intercepts search result at any point of the workflow execution.
 * <blockquote><pre>
 *  workflowVerifier.prepareRun(myWorkflow)
 *        .doSearch(SearchWfSpecRequest.class, searchResultCaptor.capture(), searchRequest)
 *        .start();
 *  CapturedResult{@code <WfSpecIdList>} capturedResult =  searchResultCaptor.getValue();
 * </pre></blockquote>
 * @param <T> Search response type.
 */
public final class SearchResultCaptor<T extends Message> {
    private final Class<T> target;
    private final AtomicInteger currentIndex = new AtomicInteger();

    private final List<CapturedResult<T>> results = new ArrayList<>();

    private SearchResultCaptor(final Class<T> target) {
        this.target = target;
    }

    public static <T extends Message> SearchResultCaptor<T> of(final Class<T> target) {
        return new SearchResultCaptor<>(target);
    }

    /**
     * Captures the result within a {@code Step}
     * @return search response
     */
    public CapturedResult<T> capture() {
        CapturedResult<T> captured = new CapturedResultImpl(target);
        results.add(captured);
        return captured;
    }

    /**
     * Ignores the result
     */
    public CapturedResult<T> skip() {
        SkipResult skipResult = new SkipResult();
        results.add(skipResult);
        return skipResult;
    }

    /**
     * Returns current value.
     * Note: this method guarantees the order of the values during the execution.
     * @return search response
     */
    public CapturedResult<T> getValue() {
        CapturedResult<T> result;
        do {
            final int index = currentIndex.getAndIncrement();
            if (index < results.size()) {
                result = results.get(index);
            } else {
                result = null;
            }
        } while (result == null || result.getClass().isAssignableFrom(SkipResult.class));
        return result;
    }

    private final class SkipResult implements CapturedResult<T> {

        @Override
        public T get() {
            throw new UnsupportedOperationException();
        }

        public void set(T result) {
            // skip
        }

        @Override
        public Class<T> type() {
            return target;
        }
    }
}
