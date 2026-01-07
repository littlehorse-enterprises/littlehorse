package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.Timestamp;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.SleepNode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.Test;

class SleepNodeModelTest {

    private final VariableAssignmentModel assignment = mock(VariableAssignmentModel.class);
    private final ThreadRunModel threadRun = mock(ThreadRunModel.class);
    private final ZoneOffset defaultZoneOffset =
            ZoneId.systemDefault().getRules().getOffset(Instant.now());

    @Test
    public void shouldCalculateMaturationTimeBasedOnRawSeconds() throws LHVarSubError {
        LocalDateTime expectedMaturationTime = LocalDateTime.now().plusDays(10);
        //        Timestamp sleepUntilTimestamp =
        // Timestamp.newBuilder().setSeconds(expectedMaturationTime.toEpochSecond(defaultZoneOffset)).build();
        when(threadRun.assignVariable(assignment))
                .thenReturn(new VariableValueModel(Duration.ofDays(10).toSeconds()));
        SleepNodeModel sleepNode = new SleepNodeModel();
        sleepNode.type = SleepNode.SleepLengthCase.RAW_SECONDS;
        sleepNode.rawSeconds = assignment;
        Date maturationTime = sleepNode.getMaturationTime(threadRun);
        assertThat(maturationTime)
                .isCloseTo(
                        expectedMaturationTime.toInstant(defaultZoneOffset),
                        Duration.ofSeconds(2).toMillis());
    }

    @Test
    public void shouldCalculateMaturationTimeISODateString() throws LHVarSubError {
        Date expectedMaturationTime =
                new Date(Instant.parse("2026-12-31T00:00:00.000Z").toEpochMilli());
        when(threadRun.assignVariable(assignment)).thenReturn(new VariableValueModel("2026-12-31T00:00:00.000Z"));
        SleepNodeModel sleepNode = new SleepNodeModel();
        sleepNode.type = SleepNode.SleepLengthCase.ISO_DATE;
        sleepNode.isoDate = assignment;
        Date maturationTime = sleepNode.getMaturationTime(threadRun);
        assertThat(maturationTime).isEqualToIgnoringHours(expectedMaturationTime);
    }

    @Test
    public void shouldThrowLHVarSubErrorWhenInvalidDateFormat() throws LHVarSubError {
        when(threadRun.assignVariable(assignment)).thenReturn(new VariableValueModel("invalid-date-format"));
        SleepNodeModel sleepNode = new SleepNodeModel();
        sleepNode.type = SleepNode.SleepLengthCase.ISO_DATE;
        sleepNode.isoDate = assignment;

        assertThatThrownBy(() -> sleepNode.getMaturationTime(threadRun)).isInstanceOf(LHVarSubError.class);
    }

    @Test
    public void shouldCalculateMaturationTimeBasedFixedTimestamp() throws LHVarSubError {
        LocalDateTime expectedMaturationTime = LocalDateTime.now().plusDays(10);
        Timestamp sleepUntilTimestamp = Timestamp.newBuilder()
                .setSeconds(expectedMaturationTime.toEpochSecond(defaultZoneOffset))
                .build();
        when(threadRun.assignVariable(assignment)).thenReturn(new VariableValueModel(sleepUntilTimestamp));
        SleepNodeModel sleepNode = new SleepNodeModel();
        sleepNode.type = SleepNode.SleepLengthCase.TIMESTAMP;
        sleepNode.timestamp = assignment;
        Date maturationTime = sleepNode.getMaturationTime(threadRun);
        assertThat(maturationTime).isEqualToIgnoringMillis(expectedMaturationTime.toInstant(defaultZoneOffset));
    }
}
