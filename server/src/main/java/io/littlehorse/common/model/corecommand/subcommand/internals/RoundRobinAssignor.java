package io.littlehorse.common.model.corecommand.subcommand.internals;

import com.google.common.collect.Iterables;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerMetadataModel;
import java.util.*;

public class RoundRobinAssignor implements TaskWorkerAssignor {

    @Override
    public void assign(Collection<HostModel> hosts, Collection<TaskWorkerMetadataModel> taskWorkers) {
        // Ensure idempotency
        Set<HostModel> sortedHosts = new TreeSet<>(hosts);
        Set<TaskWorkerMetadataModel> sortedTaskWorkers = new TreeSet<>(taskWorkers);

        // Remove old assignment
        sortedTaskWorkers.forEach(worker -> worker.hosts.clear());

        // Create a circular list
        Iterator<TaskWorkerMetadataModel> roundRobinWorkers =
                Iterables.cycle(sortedTaskWorkers).iterator();

        // Control collection, it is needed to assign remaining worker to a server
        List<TaskWorkerMetadataModel> remainingWorkers = new ArrayList<>(sortedTaskWorkers);

        // Assigning N workers to a server
        for (HostModel host : sortedHosts) {
            TaskWorkerMetadataModel worker = roundRobinWorkers.next();
            remainingWorkers.remove(worker);
            worker.hosts.add(host);
        }

        // Assign remaining workers to a server
        if (!remainingWorkers.isEmpty()) {
            Iterator<HostModel> roundRobinHost = Iterables.cycle(sortedHosts).iterator();
            for (TaskWorkerMetadataModel worker : remainingWorkers) {
                worker.hosts.add(roundRobinHost.next());
            }
        }
    }
}
