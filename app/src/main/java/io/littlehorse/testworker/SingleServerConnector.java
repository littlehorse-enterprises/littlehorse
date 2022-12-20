package io.littlehorse.testworker;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.proto.LHPublicApiGrpc;
import io.littlehorse.common.proto.LHPublicApiGrpc.LHPublicApiStub;
import io.littlehorse.common.proto.PollTaskPb;
import io.littlehorse.common.proto.PollTaskReplyPb;
import io.littlehorse.common.proto.ReportTaskReplyPb;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.TaskResultEventPb;
import io.littlehorse.common.proto.TaskScheduleRequestPb;
import io.littlehorse.common.proto.VariableValuePb;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class SingleServerConnector implements StreamObserver<PollTaskReplyPb> {

    private String taskDefName;
    private String clientId;
    private String host;
    private int port;
    private boolean stillRunning;
    private TaskFunc func;
    private StreamObserver<PollTaskPb> pollClient;
    private LHPublicApiStub stub;

    public SingleServerConnector(
        TaskFunc func,
        String host,
        int port,
        String taskDefName,
        String clientId
    ) {
        System.out.println("Constructor: " + host + ":" + port);
        this.func = func;
        this.host = host;
        this.port = port;
        this.taskDefName = taskDefName;
        this.clientId = clientId;

        stillRunning = true;
        Channel channel = ManagedChannelBuilder
            .forAddress(this.host, this.port)
            .usePlaintext()
            .build();

        this.stub = LHPublicApiGrpc.newStub(channel);
        this.pollClient = stub.pollTask(this);

        System.out.println("Hi from constructor SSC");
        // kick off the party
        for (int i = 0; i < 5; i++) {
            pollClient.onNext(
                PollTaskPb
                    .newBuilder()
                    .setClientId(clientId)
                    .setTaskDefName(taskDefName)
                    .build()
            );
        }
        System.out.println("Got back from the first request async send");
    }

    public boolean matches(String host, int port) {
        return this.host.equals(host) && this.port == port;
    }

    public void stop() {
        stillRunning = false;
    }

    public void onCompleted() {
        System.out.println("TODO: remove from parents");
    }

    public void onNext(PollTaskReplyPb taskToDo) {
        System.out.println("Doing an onNext()");
        if (taskToDo.hasResult()) {
            System.out.println("Hello there we got something!");
            TaskScheduleRequestPb tsr = taskToDo.getResult();

            VariableValuePb result = func.execute(tsr);
            stub.reportTask(
                TaskResultEventPb
                    .newBuilder()
                    .setWfRunId(tsr.getWfRunId())
                    .setThreadRunNumber(tsr.getThreadRunNumber())
                    .setTaskRunPosition(tsr.getTaskRunPosition())
                    .setTime(LHUtil.fromDate(new Date()))
                    .setResultCode(TaskResultCodePb.SUCCESS)
                    .setOutput(result)
                    .setFromRpc(true)
                    .build(),
                new StreamObserver<ReportTaskReplyPb>() {
                    @Override
                    public void onError(Throwable t) {
                        LHUtil.log("yikes, got an error:");
                        t.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onNext(ReportTaskReplyPb reply) {}
                }
            );
            System.out.println("Done replying");
        } else {
            LHUtil.log("hmmm", taskToDo.getCode().toString(), taskToDo.getMessage());
        }

        if (stillRunning) {
            System.out.println("Client id is:" + clientId);
            System.out.println("TaskDef is  :" + taskDefName);
            pollClient.onNext(
                PollTaskPb
                    .newBuilder()
                    .setClientId(clientId)
                    .setTaskDefName(taskDefName)
                    .build()
            );
        }
    }

    public void onError(Throwable t) {
        System.out.println("Unexpected error");
        t.printStackTrace();
        stillRunning = false;
    }
}
