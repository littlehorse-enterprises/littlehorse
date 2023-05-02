package io.littlehorse.common.model.command.subcommand.internals;

import io.littlehorse.common.model.meta.Host;
import io.littlehorse.common.model.meta.TaskWorkerMetadata;
import java.util.Collection;

public interface TaskWorkerAssignor {
    void assign(Collection<Host> hosts, Collection<TaskWorkerMetadata> taskWorkers);
}
