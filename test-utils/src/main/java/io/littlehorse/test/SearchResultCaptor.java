package io.littlehorse.test;

import com.google.protobuf.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public CapturedResult<T> capture() {
        CapturedResult<T> captured = new CapturedResultImpl();
        results.add(captured);
        return captured;
    }

    public CapturedResult<T> skip() {
        SkipResult skipResult = new SkipResult();
        results.add(skipResult);
        return skipResult;
    }

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

    private final class CapturedResultImpl implements CapturedResult<T> {
        private T value;

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T result) {
            if (value != null) {
                throw new IllegalStateException("Already has value");
            }
            value = result;
        }

        @Override
        public Class<T> type() {
            return target;
        }
    }

    private final class SkipResult implements CapturedResult<T> {

        @Override
        public T get() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T result) {
            // skip
        }

        @Override
        public Class<T> type() {
            return target;
        }
    }
}
