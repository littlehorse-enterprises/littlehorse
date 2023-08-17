package io.littlehorse.sdk.worker.internal.util;

import io.grpc.stub.StreamObserver;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.ReportTaskReplyPb;
import io.littlehorse.sdk.common.proto.ReportTaskRun;
import io.littlehorse.sdk.worker.internal.LHServerConnectionManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportTaskObserver implements StreamObserver<ReportTaskReplyPb> {

    private LHServerConnectionManager manager;
    private ReportTaskRun reportedTask;
    private int retriesLeft;

    public ReportTaskObserver(
        LHServerConnectionManager manager,
        ReportTaskRun reportedTask,
        int retriesLeft
    ) {
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

    public void onNext(ReportTaskReplyPb reply) {
        if (reply.getCode() == LHResponseCode.OK) {
            // Nothing to do
        } else if (reply.getCode() == LHResponseCode.REPORTED_BUT_NOT_PROCESSED) {
            log.warn("Reported task but processor was down. No action required");
            // Nothing to do
        } else {
            // Failed to report, should retry.
            log.error("Error reporting task: {}", reply.getCode());
            if (reportedTask != null) {
                manager.retryReportTask(reportedTask, retriesLeft - 1);
            }
        }
    }
}
