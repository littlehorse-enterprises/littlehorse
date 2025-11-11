package io.littlehorse.sdk.worker.internal;

import com.google.common.collect.Iterators;
import java.io.Closeable;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PollThread extends Thread implements Closeable {

    private final Iterator<PollTaskStub> activePollClientsIterator;
    private boolean stillRunning = true;
    private final boolean requireConcurrency;
    private final List<PollTaskStub> pollClients;

    PollThread(String threadName, int inflightRequests, List<PollTaskStub> pollClients) {
        super(threadName);
        this.requireConcurrency = inflightRequests > 1;
        this.pollClients = pollClients;
        this.activePollClientsIterator = Iterators.cycle(pollClients);
    }

    @Override
    public void run() {
        try {
            while (stillRunning) {
                PollTaskStub pollClient = activePollClientsIterator.next();
                if (!requireConcurrency || pollClient.isReady()) {
                    pollClient.doNext();
                } else {
                    // stop requests until it receives enough responses
                    pollClient.acquireNextPermit();
                }
                if (pollClient.isClosed()) {
                    stillRunning = false;
                }
            }
        } catch (InterruptedException ex) {
            log.debug("Thread interrupted");
        } finally {
            log.debug(String.format("Thread %s stopped", getName()));
            close();
        }
    }

    @Override
    public void close() {
        this.stillRunning = false;
        for (PollTaskStub pollClient : pollClients) {
            pollClient.close();
        }
    }

    public boolean isRunning() {
        checkRunningStubs();
        return stillRunning;
    }

    private void checkRunningStubs() {
        if (!stillRunning) return;
        boolean oneStubFailed = pollClients.stream().anyMatch(PollTaskStub::isClosed);
        if (oneStubFailed) {
            close();
        }
    }
}
