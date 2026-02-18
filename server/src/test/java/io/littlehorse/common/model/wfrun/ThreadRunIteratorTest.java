package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.common.model.getable.core.wfrun.InactiveThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunIterator;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.InactiveThreadRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

public class ThreadRunIteratorTest {

    @Test
    void shouldIncludeGreatestThreadRunNumber() {
        ThreadRunModel thread0 = new ThreadRunModel();
        thread0.setNumber(0);
        ThreadRunModel thread1 = new ThreadRunModel();
        thread1.setNumber(1);

        ThreadRunIterator iterator = new ThreadRunIterator(new WfRunIdModel("wf"), List.of(thread0, thread1), 1, null);

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isSameAs(thread0);
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isSameAs(thread1);
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void shouldLoadArchivedThreadRunWhenNotInMemory() {
        WfRunIdModel wfRunId = new WfRunIdModel("wf");

        ThreadRunModel thread0 = new ThreadRunModel();
        thread0.setNumber(0);

        WfRunModel wfRun = new WfRunModel();
        wfRun.setId(wfRunId);

        ThreadRunModel archivedThread = new ThreadRunModel();
        archivedThread.setNumber(1);
        archivedThread.setWfRun(wfRun);

        InactiveThreadRunModel inactive = new InactiveThreadRunModel(archivedThread);

        GetableManager getableManager = mock(GetableManager.class);
        when(getableManager.get(any(InactiveThreadRunIdModel.class))).thenReturn(inactive);

        ThreadRunIterator iterator = new ThreadRunIterator(wfRunId, List.of(thread0), 1, getableManager);

        assertThat(iterator.next()).isSameAs(thread0);
        assertThat(iterator.next()).isSameAs(archivedThread);
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void shouldThrowWhenNextCalledAfterEnd() {
        ThreadRunModel thread0 = new ThreadRunModel();
        thread0.setNumber(0);

        ThreadRunIterator iterator = new ThreadRunIterator(new WfRunIdModel("wf"), List.of(thread0), 0, null);

        assertThat(iterator.next()).isSameAs(thread0);
        assertThat(iterator.hasNext()).isFalse();
        assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
    }
}
