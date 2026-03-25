package io.littlehorse.server.streams;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractCommand;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.TaskClaimEventModel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class LHFuture<T> extends CompletableFuture<T> {

    protected LHFuture() {}

    public static LHFuture<Message> forCommand(AbstractCommand<?> cmd) {
        if (cmd instanceof CommandModel coreCommand && coreCommand.getSubCommand() instanceof TaskClaimEventModel) {
            return new TaskClaimFuture();
        }
        return new LHFuture<>();
    }

    static final class TaskClaimFuture extends LHFuture<Message> {

        private TaskClaimFuture() {
            // Given that under rebalances, the ScheduledTask may be reassigned to another instance, we set a timeout
            // for the task to be claimed.
            // This also fails the task when there is lag in the Command processors. 10 seconds is a reasonable amount
            // of time.
            // The future will complete exceptionally if the task isn't claimed by this time, causing the TaskRun to be
            // marked as TIMEOUT.
            orTimeout(10, TimeUnit.SECONDS);
        }
    }
}
