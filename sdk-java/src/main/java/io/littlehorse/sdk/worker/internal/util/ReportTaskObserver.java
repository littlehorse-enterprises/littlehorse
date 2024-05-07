package io.littlehorse.sdk.worker.internal.util;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.worker.internal.LHServerConnectionManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportTaskObserver implements StreamObserver<Empty> {

    private LHServerConnectionManager manager;
    private ReportTaskRun reportedTask;
    private int retriesLeft;

    public ReportTaskObserver(LHServerConnectionManager manager, ReportTaskRun reportedTask, int retriesLeft) {
        this.manager = manager;
        this.reportedTask = reportedTask;
        this.retriesLeft = retriesLeft;
    }

    public void onCompleted() {
        // Nothing to do
    }

    public void onError(Throwable t) {
        if (retriesLeft > 0) {
            log.error("ReportTask failed, enqueuing retry");
            manager.retryReportTask(reportedTask, retriesLeft - 1);
        } else {
            log.error("ReportTask failed, not enqueueing retry");
            t.printStackTrace();
        }
    }

    public void onNext(Empty reply) {}
}
