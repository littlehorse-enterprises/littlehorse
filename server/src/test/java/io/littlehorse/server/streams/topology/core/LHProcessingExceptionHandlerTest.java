package io.littlehorse.server.streams.topology.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.grpc.StatusRuntimeException;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.RunWfRequestModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutWfSpecRequestModel;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.util.AsyncWaiters;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.common.errors.InvalidProducerEpochException;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.logging.log4j.message.Message;
import org.junit.jupiter.api.Test;

public class LHProcessingExceptionHandlerTest {

    private final LHServer mockServer = mock();
    private final AsyncWaiters asyncWaiters = mock();
    private final LHProcessingExceptionHandler exceptionHandler =
            new LHProcessingExceptionHandler(mockServer, asyncWaiters);
    private final CompletableFuture<Message> futureResponse = new CompletableFuture<>();

    @Test
    public void shouldIgnoreRecordTooLargeException() {
        exceptionHandler.tryRun(() -> {
            throw new RecordTooLargeException("record too large");
        });
    }

    @Test
    public void shouldPropagateKafkaExceptions() {
        assertThatThrownBy(() -> {
                    throw new InvalidProducerEpochException("invalid epoch");
                })
                .isInstanceOf(InvalidProducerEpochException.class);
    }

    @Test
    public void shouldHandleUserValidationExceptions() {
        CommandModel command = new CommandModel(new RunWfRequestModel());
        command.setCommandId("myCommand");
        StatusRuntimeException sre = new StatusRuntimeException(io.grpc.Status.INVALID_ARGUMENT);
        CoreCommandException cce = new CoreCommandException(sre, command);
        when(asyncWaiters.getOrRegisterFuture(eq(command.getCommandId().get()), any(), any(CompletableFuture.class)))
                .thenReturn(futureResponse);
        exceptionHandler.tryRun(() -> {
            throw cce;
        });
        assertThatThrownBy(() -> futureResponse.getNow(null)).hasCauseInstanceOf(StatusRuntimeException.class);
    }

    @Test
    public void shouldHandleCoreCommandRuntimeExceptions() {
        CommandModel command = new CommandModel(new RunWfRequestModel());
        command.setCommandId("myCommand");
        NullPointerException npe = new NullPointerException();
        CoreCommandException cce = new CoreCommandException(npe, command);
        when(asyncWaiters.getOrRegisterFuture(eq(command.getCommandId().get()), any(), any(CompletableFuture.class)))
                .thenReturn(futureResponse);
        exceptionHandler.tryRun(() -> {
            throw cce;
        });
        assertThatThrownBy(() -> futureResponse.getNow(null)).hasCauseInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldHandleMetadataCommandRuntimeExceptions() {
        MetadataCommandModel command = new MetadataCommandModel(new PutWfSpecRequestModel());
        command.setCommandId("myCommand");
        NullPointerException npe = new NullPointerException();
        MetadataCommandException mce = new MetadataCommandException(npe, command);
        when(asyncWaiters.getOrRegisterFuture(eq(command.getCommandId().get()), any(), any(CompletableFuture.class)))
                .thenReturn(futureResponse);
        exceptionHandler.tryRun(() -> {
            throw mce;
        });
        assertThatThrownBy(() -> futureResponse.getNow(null)).hasCauseInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldHandleMetadataUserValidationExceptions() {
        MetadataCommandModel command = new MetadataCommandModel(new PutWfSpecRequestModel());
        command.setCommandId("myCommand");
        StatusRuntimeException sre = new StatusRuntimeException(io.grpc.Status.INVALID_ARGUMENT);
        MetadataCommandException mce = new MetadataCommandException(sre, command);
        when(asyncWaiters.getOrRegisterFuture(eq(command.getCommandId().get()), any(), any(CompletableFuture.class)))
                .thenReturn(futureResponse);
        exceptionHandler.tryRun(() -> {
            throw mce;
        });
        assertThatThrownBy(() -> futureResponse.getNow(null)).hasCauseInstanceOf(StatusRuntimeException.class);
    }
}
