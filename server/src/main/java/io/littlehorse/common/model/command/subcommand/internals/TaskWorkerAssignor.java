package io.littlehorse.common.model.command.subcommand.internals;

import io.littlehorse.common.model.meta.HostModel;
import io.littlehorse.common.model.meta.TaskWorkerMetadataModel;
import java.util.Collection;

public interface TaskWorkerAssignor {
    void assign(
        Collection<HostModel> hosts,
        Collection<TaskWorkerMetadataModel> taskWorkers
    );
}
