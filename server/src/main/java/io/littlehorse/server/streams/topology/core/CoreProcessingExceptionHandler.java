package io.littlehorse.server.streams.topology.core;

import io.grpc.StatusRuntimeException;
import io.littlehorse.server.LHServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.RecordTooLargeException;

@Slf4j
public class CoreProcessingExceptionHandler {

    private final LHServer server;

    public CoreProcessingExceptionHandler(LHServer server) {
        this.server = server;
    }

    public void tryRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (CoreCommandException commandException) {
            if (commandException.isUserError()) {
                StatusRuntimeException sre = (StatusRuntimeException) commandException.getCause();
                log.debug(
                        "Caught exception processing {}:\nStatus: {}\nDescription: {}\nCause: {}",
                        commandException.getCommand().getType(),
                        sre.getStatus().getCode(),
                        sre.getStatus().getDescription(),
                        sre.getMessage(),
                        sre.getCause());
            } else {
                log.error(
                        "Caught exception processing {} command:",
                        commandException.getCommand().getType(),
                        commandException.getCause());
            }
            if (commandException.isSendErrorToClient()) {
                try {
                    server.sendErrorToClient(commandException.getCommand().getCommandId(), commandException.getCause());
                } catch (Exception e) {
                    // Nothing to do
                }
            }
        } catch (RecordTooLargeException rtle) {
            log.error("Unexpected error processing record: ", rtle);
        } catch (KafkaException ke) {
            throw ke;
        } catch (Throwable ex) {
            log.error("Unexpected error processing record: ", ex);
        }
    }
}
