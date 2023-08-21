package io.littlehorse.common.model.command.subcommand.internals;

import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerMetadataModel;
import java.util.Collection;

public interface TaskWorkerAssignor {
    void assign(Collection<HostModel> hosts, Collection<TaskWorkerMetadataModel> taskWorkers);
}
