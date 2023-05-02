package io.littlehorse.common.model.command.subcommand.internals;

import com.google.common.collect.Iterables;
import io.littlehorse.common.model.meta.Host;
import io.littlehorse.common.model.meta.TaskWorkerMetadata;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

public class RoundRobinAssignor implements TaskWorkerAssignor {

    private static final int MIN_WORKER_ASSIGNMENT_BY_SERVER = 2;

    @Override
    public void assign(
        Collection<Host> hosts,
        Collection<TaskWorkerMetadata> taskWorkers
    ) {
        // Remove old assignment
        taskWorkers.forEach(worker -> worker.hosts.clear());

        // Create a circular list
        Iterator<TaskWorkerMetadata> roundRobinWorkers = Iterables
            .cycle(taskWorkers)
            .iterator();

        // Control collection, it is needed to assign remaining worker to a server
        List<TaskWorkerMetadata> remainingWorkers = new ArrayList<>(taskWorkers);

        // Assigning N workers to a server
        for (Host host : hosts) {
            IntStream
                .range(0, MIN_WORKER_ASSIGNMENT_BY_SERVER)
                .forEach(i -> {
                    TaskWorkerMetadata worker = roundRobinWorkers.next();
                    remainingWorkers.remove(worker);
                    worker.hosts.add(host);
                });
        }

        // Assign remaining workers to a server
        if (!remainingWorkers.isEmpty()) {
            Iterator<Host> roundRobinHost = Iterables.cycle(hosts).iterator();
            for (TaskWorkerMetadata worker : remainingWorkers) {
                worker.hosts.add(roundRobinHost.next());
            }
        }
    }
}
