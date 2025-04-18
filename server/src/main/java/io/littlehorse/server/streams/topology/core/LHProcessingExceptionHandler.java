package io.littlehorse.server.streams.topology.core;

import io.grpc.StatusRuntimeException;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.CommandSender;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.RecordTooLargeException;

// This class will implement ProcessingException handler on Kafka Streams 3.9
// See KIP-1033
@Slf4j
public class LHProcessingExceptionHandler {

    private final LHServer server;
    private final ConcurrentHashMap<String, CommandSender.FutureAndType> asyncCompletables;

    public LHProcessingExceptionHandler(
            LHServer server, ConcurrentHashMap<String, CommandSender.FutureAndType> asyncCompletables) {
        this.server = server;
        this.asyncCompletables = asyncCompletables;
    }

    public void tryRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (CoreCommandException commandException) {
            if (commandException.isUserError()) {
                // debug and continue
                StatusRuntimeException sre = (StatusRuntimeException) commandException.getCause();
                log.debug(
                        "Caught exception processing {}:\nStatus: {}\nDescription: {}\nCause: {}",
                        commandException.getCommand().getType(),
                        sre.getStatus().getCode(),
                        sre.getStatus().getDescription(),
                        sre.getMessage(),
                        sre.getCause());
            } else {
                // Log and continue
                log.error(
                        "Caught exception processing {} command:",
                        commandException.getCommand().getType(),
                        commandException.getCause());
            }
            if (commandException.isNotifyClientOnError()) {
                asyncCompletables
                        .get(commandException.getCommand().getCommandId())
                        .completable()
                        .completeExceptionally(commandException.getCause());
            }
        } catch (MetadataCommandException ex) {
            if (ex.isUserError()) {
                log.trace(
                        "Sending exception when processing command {}: {}",
                        ex.getCommand().getType(),
                        ex.getCause().getMessage());
            } else {
                log.error(
                        "Caught exception processing {} command: {}",
                        ex.getCommand().getType(),
                        ex.getCause());
            }
            try {
                asyncCompletables
                        .get(ex.getCommand().getCommandId())
                        .completable()
                        .completeExceptionally(ex.getCause());
            } catch (Exception e) {
                // Nothing to do
            }
        } catch (RecordTooLargeException rtle) {
            // Log and continue. This will include a record-dropped metric
            log.error("Record dropped: ", rtle);
        } catch (KafkaException ke) {
            // Time to notify kafka streams that something went wrong (i.e InvalidEpochException during commit)
            throw ke;
        } catch (Throwable ex) {
            // Log and continue
            log.error("Unexpected error processing record: ", ex);
        }
    }
}
