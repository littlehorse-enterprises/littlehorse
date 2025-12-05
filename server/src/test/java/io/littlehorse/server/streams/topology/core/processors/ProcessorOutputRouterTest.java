package io.littlehorse.server.streams.topology.core.processors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.proto.Command;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.server.streams.ServerTopologyV2;
import java.util.Date;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ProcessorOutputRouterTest {

    private final ProcessorContext<String, Object> context = mock(ProcessorContext.class);
    private final Record<String, Object> record = new Record<>("test", new Object(), 1L);

    @Test
    public void shouldPassCoreCommandFromTimerToTimerCoreCommandProcessor() throws InvalidProtocolBufferException {
        Command command = runWfCommand();
        LHTimer timerCommand = new LHTimer(CommandModel.fromProto(command, CommandModel.class, mock()));
        ProcessorOutputRouter<String, LHTimer, String, Object> passthroughRepartitionRouter =
                ProcessorOutputRouter.createTimerProcessorRouter();
        passthroughRepartitionRouter.init(context);
        passthroughRepartitionRouter.process(record.withValue(timerCommand));
        Record<String, Command> expectedCommand = record.withValue(command);
        ArgumentCaptor<Record<String, Command>> recordArgumentCaptor = ArgumentCaptor.forClass(Record.class);
        verify(context).forward(recordArgumentCaptor.capture(), same(ServerTopologyV2.TIMER_COMMAND_PROCESSOR_NAME));
        Record<String, Command> actualRecord = recordArgumentCaptor.getValue();
        assertThat(actualRecord.withValue(actualRecord.value())).isEqualTo(expectedCommand);
    }

    private Command runWfCommand() {
        RunWfRequest runWfRequest = RunWfRequest.newBuilder().setId("test").build();
        return Command.newBuilder()
                .setTime(LHLibUtil.fromDate(new Date()))
                .setRunWf(runWfRequest)
                .build();
    }
}
