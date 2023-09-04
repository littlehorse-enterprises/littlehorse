package io.littlehorse.common.model.getable.core.wfrun.subnoderun;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.utils.WaitForThreadModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class WaitForThreadsRunModelTest {

    @Spy
    private final WaitForThreadsRunModel waitForThreadsRunModel = new WaitForThreadsRunModel();

    private final WfRunModel mockWfRunModel = mock(WfRunModel.class);

    private final ThreadRunModel firstThreadRunModel = mock(ThreadRunModel.class, Answers.RETURNS_DEEP_STUBS);
    private final ThreadRunModel secondThreadRunModel = mock(ThreadRunModel.class, Answers.RETURNS_DEEP_STUBS);

    private final WaitForThreadModel firstWaitForThread = mock(WaitForThreadModel.class);
    private final WaitForThreadModel secondWaitForThread = mock(WaitForThreadModel.class);

    private final NodeRunModel mockParentNodeRun = mock(NodeRunModel.class);

    private final Date advanceDate = new Date();

    private final List<WaitForThreadModel> waitForThreadModels = List.of(firstWaitForThread, secondWaitForThread);

    @BeforeEach
    public void setup() {
        doReturn(mockWfRunModel).when(waitForThreadsRunModel).getWfRun();
        doAnswer(invocation -> resolveMockThreadRunModel(invocation.getArgument(0)))
                .when(mockWfRunModel)
                .getThreadRun(anyInt());
        when(firstWaitForThread.getThreadRunNumber()).thenReturn(1);
        when(secondWaitForThread.getThreadRunNumber()).thenReturn(2);
        waitForThreadsRunModel.setThreads(waitForThreadModels);
        waitForThreadsRunModel.setNodeRunModel(mockParentNodeRun);
    }

    private ThreadRunModel resolveMockThreadRunModel(Integer threadRunNumber) {
        return switch (threadRunNumber) {
            case 1:
                yield firstThreadRunModel;
            case 2:
                yield secondThreadRunModel;
            default:
                throw new IllegalArgumentException(
                        "There is no ThreadRunModel associated to %s number".formatted(threadRunNumber));
        };
    }

    @Test
    void shouldUpdateWaitForThreadModelStatus() {
        waitForThreadsRunModel.advanceIfPossible(advanceDate);
        verify(firstWaitForThread).setThreadStatus(firstThreadRunModel.getStatus());
    }

    @Nested
    class AllChildrenThreadsCompleted {

        @BeforeEach
        void setup() {
            when(firstThreadRunModel.isTerminated()).thenReturn(true);
            when(secondThreadRunModel.isTerminated()).thenReturn(true);
            waitForThreadsRunModel.setThreads(List.of(firstWaitForThread, secondWaitForThread));
        }

        @Test
        void shouldAdvance() {
            ArgumentCaptor<VariableValueModel> variableValueCaptor = ArgumentCaptor.forClass(VariableValueModel.class);
            boolean shouldAdvance = waitForThreadsRunModel.advanceIfPossible(advanceDate);
            assertThat(shouldAdvance).isTrue();
            verify(waitForThreadsRunModel.getNodeRunModel()).complete(variableValueCaptor.capture(), eq(advanceDate));
            VariableValueModel variableValueOutput = variableValueCaptor.getValue();
            assertThat(variableValueOutput)
                    .isNotNull()
                    .extracting(VariableValueModel::getType)
                    .isEqualTo(VariableType.NULL);
        }

        @Test
        void shouldMarksAsHandledAndSetEndTime() {
            waitForThreadsRunModel.advanceIfPossible(advanceDate);
            verify(firstWaitForThread).setThreadEndTime(advanceDate);
            verify(secondWaitForThread).setThreadEndTime(advanceDate);
            verify(firstWaitForThread).setAlreadyHandled(true);
            verify(secondWaitForThread).setAlreadyHandled(true);
        }

        @Test
        void shouldHandleErrors() {
            when(secondWaitForThread.getThreadStatus()).thenReturn(LHStatus.ERROR);
            waitForThreadsRunModel.advanceIfPossible(advanceDate);
            verify(waitForThreadsRunModel.getNodeRunModel()).fail(any(), eq(advanceDate));
            verify(waitForThreadsRunModel.getNodeRunModel(), never()).complete(any(), any());
        }
    }

    @Nested
    class ChildrenThreadsStillRunning {
        @BeforeEach
        void setup() {
            when(firstThreadRunModel.isTerminated()).thenReturn(false);
            when(secondThreadRunModel.isTerminated()).thenReturn(true);
        }

        @Test
        void shouldNotAdvanceIfSomeChildIsNotTerminated() {
            boolean shouldAdvance = waitForThreadsRunModel.advanceIfPossible(advanceDate);
            assertThat(shouldAdvance).isFalse();
            verify(waitForThreadsRunModel.getNodeRunModel(), never()).complete(any(), eq(advanceDate));
            verify(secondWaitForThread, times(1)).setAlreadyHandled(true);
        }

        @Test
        void shouldNotMarkWaitingThreadAsHandled() {
            waitForThreadsRunModel.advanceIfPossible(advanceDate);
            verify(firstWaitForThread, never()).setAlreadyHandled(true);
            verify(firstWaitForThread, never()).setThreadEndTime(any());
        }
    }

    @Nested
    class PropagateException {

        @Mock
        private final FailureModel nodeFailure = mock(FailureModel.class);

        private final int failureNodePosition = new Random().nextInt();

        @BeforeEach
        void setup() {
            Stream.of(firstThreadRunModel, secondThreadRunModel)
                    .forEach(threadRun -> when(threadRun.isTerminated()).thenReturn(true));
            when(firstWaitForThread.getThreadStatus()).thenReturn(LHStatus.COMPLETED);
            when(secondWaitForThread.getThreadStatus()).thenReturn(LHStatus.EXCEPTION);
            when(secondThreadRunModel.getCurrentNodePosition()).thenReturn(failureNodePosition);
            when(secondThreadRunModel
                            .getNodeRun(secondThreadRunModel.getCurrentNodePosition())
                            .getLatestFailure())
                    .thenReturn(nodeFailure);
        }

        @Test
        void shouldPropagateExceptionFromChildToParentNode() {
            boolean shouldAdvance = waitForThreadsRunModel.advanceIfPossible(advanceDate);
            assertThat(shouldAdvance).isTrue();
            verify(waitForThreadsRunModel.getNodeRunModel()).fail(eq(nodeFailure), eq(advanceDate));
        }

        @Test
        void shouldPropagateExceptionFromChildToParentWhenThereIsANonTerminatedWaitThread() {
            when(firstThreadRunModel.isTerminated()).thenReturn(false);
            boolean shouldAdvance = waitForThreadsRunModel.advanceIfPossible(advanceDate);
            assertThat(shouldAdvance).isFalse();
            verify(waitForThreadsRunModel.getNodeRunModel()).fail(eq(nodeFailure), eq(advanceDate));
        }

        @Test
        void shouldNotPropagateExceptionTwice() {
            when(firstWaitForThread.isAlreadyHandled()).thenReturn(true);
            when(secondWaitForThread.isAlreadyHandled()).thenReturn(true);
            waitForThreadsRunModel.advanceIfPossible(advanceDate);
            verify(waitForThreadsRunModel.getNodeRunModel(), never()).fail(eq(nodeFailure), eq(advanceDate));
        }
    }
}
