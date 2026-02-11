package io.littlehorse.common.model.corecommand.subcommand;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.sdk.common.proto.ScheduleWfRequest;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.LHTaskManager;
import io.littlehorse.server.streams.topology.core.WfService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

class ScheduleWfRequestModelTest {

    @ParameterizedTest
    @MethodSource("scheduleFirstRunCases")
    void shouldScheduleFirstRunAtNextCronDate(String cronExpression, LocalDateTime expectedDate) {
        ScheduleWfRequestModel model = new ScheduleWfRequestModel();

        // Setup mock data
        String wfSpecName = "my-wf";

        // Mocking dependencies
        CoreProcessorContext context = mock(CoreProcessorContext.class);
        WfService wfService = mock(WfService.class);
        LHTaskManager taskManager = mock(LHTaskManager.class);
        GetableManager getableManager = mock(GetableManager.class);
        LHServerConfig config = mock(LHServerConfig.class);

        when(context.service()).thenReturn(wfService);
        when(context.getTaskManager()).thenReturn(taskManager);
        when(context.getableManager()).thenReturn(getableManager);

        WfSpecModel spec = mock(WfSpecModel.class);
        WfSpecIdModel specId = new WfSpecIdModel(wfSpecName, 1, 0);
        when(spec.getId()).thenReturn(specId);
        when(wfService.getWfSpec(wfSpecName, null, null)).thenReturn(spec);

        // Initialize model manually since we're not using proto here for simplicity.
        ScheduleWfRequest proto = ScheduleWfRequest.newBuilder()
                .setId("my-id")
                .setWfSpecName(wfSpecName)
                .setCronExpression(cronExpression)
                .build();

        try {
            model.initFrom(proto, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Execute process
        model.process(context, config);

        // Capture the timer
        ArgumentCaptor<LHTimer> timerCaptor = ArgumentCaptor.forClass(LHTimer.class);
        verify(taskManager).scheduleTimer(timerCaptor.capture());

        LHTimer capturedTimer = timerCaptor.getValue();
        LocalDateTime capturedMaturationTime =
                LocalDateTime.ofInstant(capturedTimer.maturationTime.toInstant(), ZoneId.systemDefault());
        Assertions.assertThat(capturedMaturationTime.getDayOfMonth()).isEqualTo(expectedDate.getDayOfMonth());
        Assertions.assertThat(capturedMaturationTime.getMonthValue()).isEqualTo(expectedDate.getMonthValue());
        Assertions.assertThat(capturedMaturationTime.getHour()).isEqualTo(expectedDate.getHour());
        Assertions.assertThat(capturedMaturationTime.getMinute()).isEqualTo(expectedDate.getMinute());
    }

    private static Stream<Arguments> scheduleFirstRunCases() {
        return Stream.of(
                Arguments.of(
                        "5 4 10 2 *",
                        LocalDateTime.now()
                                .withDayOfMonth(10)
                                .withMonth(2)
                                .withHour(4)
                                .withMinute(5)),
                Arguments.of(
                        "0 12 1 6 *",
                        LocalDateTime.now()
                                .withDayOfMonth(1)
                                .withMonth(6)
                                .withHour(12)
                                .withMinute(0)),
                Arguments.of("30 15 * * *", LocalDateTime.now().withHour(15).withMinute(30)),
                Arguments.of(
                        "0 8 15 * *",
                        LocalDateTime.now().withDayOfMonth(15).withHour(8).withMinute(0)),
                Arguments.of(
                        "45 23 1 10 *",
                        LocalDateTime.now()
                                .withHour(23)
                                .withMinute(45)
                                .withDayOfMonth(1)
                                .withMonth(10)),
                Arguments.of(
                        "15 7 1 1 *",
                        LocalDateTime.now()
                                .withDayOfMonth(1)
                                .withMonth(1)
                                .withHour(7)
                                .withMinute(15)),
                Arguments.of(
                        "0 0 1 1 *",
                        LocalDateTime.now()
                                .withHour(0)
                                .withMinute(0)
                                .withMonth(1)
                                .withDayOfMonth(1)),
                Arguments.of(
                        "20 19 1 2 *",
                        LocalDateTime.now()
                                .withHour(19)
                                .withMinute(20)
                                .withMonth(2)
                                .withDayOfMonth(1)),
                Arguments.of(
                        "10 11 15 3 *",
                        LocalDateTime.now()
                                .withDayOfMonth(15)
                                .withMonth(3)
                                .withHour(11)
                                .withMinute(10)));
    }
}
