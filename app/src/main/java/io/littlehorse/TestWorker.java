package io.littlehorse;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.TaskClaimEvent;
import io.littlehorse.common.model.command.subcommand.TaskResultEvent;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.CommandPb.CommandCase;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.proto.VariableValuePb;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.testworker.TestWorkerGRPC;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Bytes;

/**
 * This is a shortcut, obviously.
 */
public class TestWorker {

    private KafkaConsumer<String, Bytes> cons;
    private LHProducer prod;
    private LHProducer txnProd;
    private ExecutorService threadPool;
    private LHConfig config;
    private Semaphore availThreadsSemaphore;

    private List<TaskScheduleRequest> acknowledgedTasks;
    private Map<TopicPartition, OffsetAndMetadata> offsetMap;

    public static final int NUMM_WORKER_THREADS = 12;

    public TestWorker(LHConfig config) {
        this.prod = config.getProducer();
        this.txnProd = config.getTxnProducer();

        // TODO: should use the new API where we look up consumer group id and
        // kafka topic name from the TaskDef.
        this.cons =
            config.getKafkaConsumer(
                Arrays.asList(
                    System
                        .getenv()
                        .getOrDefault("LHORSE_TASK_DEF_ID", "task-queue-task1")
                )
            );
        this.config = config;
        this.threadPool = Executors.newFixedThreadPool(NUMM_WORKER_THREADS);
        acknowledgedTasks = new ArrayList<>();
        offsetMap = new HashMap<>();
        availThreadsSemaphore = new Semaphore(NUMM_WORKER_THREADS);

        Runtime
            .getRuntime()
            .addShutdownHook(
                new Thread(() -> {
                    this.prod.close();
                    this.cons.close();
                    this.threadPool.shutdown();
                })
            );
    }

    public void run() {
        while (true) {
            ConsumerRecords<String, Bytes> records = cons.poll(Duration.ofMillis(50));

            acknowledgedTasks.clear();
            offsetMap.clear();
            txnProd.beginTransaction();
            try {
                Iterator<ConsumerRecord<String, Bytes>> iter = records.iterator();
                while (iter.hasNext()) {
                    ConsumerRecord<String, Bytes> rec = iter.next();
                    acknowledgeRequest(rec);
                }
                txnProd.sendOffsetsToTransaction(offsetMap, cons.groupMetadata());
                txnProd.commitTransaction();
            } catch (Exception exn) {
                txnProd.abortTransaction();
                LHUtil.log("Exiting loop now, things are yikerz.");
                throw new RuntimeException(exn);
            }
            enqueueAcknowledgedTasks();
        }
    }

    private void acknowledgeRequest(ConsumerRecord<String, Bytes> r) {
        try {
            availThreadsSemaphore.acquire();
        } catch (InterruptedException exn) {
            throw new RuntimeException(exn);
        }

        TaskScheduleRequest tsr;
        try {
            tsr =
                LHSerializable.fromBytes(
                    r.value().get(),
                    TaskScheduleRequest.class,
                    config
                );
        } catch (LHSerdeError exn) {
            // TODO: in production LittleHorse, we may want to throw some sort of
            // error back to the scheduler.
            exn.printStackTrace();
            return;
        }

        TaskClaimEvent se = new TaskClaimEvent();
        se.taskRunNumber = tsr.taskRunNumber;
        se.taskRunPosition = tsr.taskRunPosition;
        se.threadRunNumber = tsr.threadRunNumber;
        se.wfRunId = tsr.wfRunId;
        se.time = new Date();

        Command cmd = new Command();
        cmd.type = CommandCase.TASK_CLAIM_EVENT;
        cmd.taskClaimEvent = se;
        cmd.time = se.time;

        txnProd.send(tsr.wfRunId, cmd, tsr.wfRunEventQueue);
        acknowledgedTasks.add(tsr);

        TopicPartition partition = new TopicPartition(r.topic(), r.partition());
        OffsetAndMetadata offset = new OffsetAndMetadata(r.offset() + 1);
        offsetMap.put(partition, offset);
    }

    private void enqueueAcknowledgedTasks() {
        for (TaskScheduleRequest tsr : acknowledgedTasks) {
            this.threadPool.submit(() -> {
                    this.executeTask(tsr);
                });
        }
    }

    private void executeTask(TaskScheduleRequest tsr) {
        try {
            executeHelper(tsr);
        } catch (Exception exn) {
            exn.printStackTrace();
        }
    }

    private void executeHelper(TaskScheduleRequest tsr) throws Exception {
        TaskResultEvent ce = new TaskResultEvent();
        ce.wfRunId = tsr.wfRunId;
        ce.taskRunNumber = tsr.taskRunNumber;
        ce.taskRunPosition = tsr.taskRunPosition;
        ce.threadRunNumber = tsr.threadRunNumber;
        ce.time = new Date();

        ce.resultCode = TaskResultCodePb.SUCCESS;
        ce.stderr = null;

        ce.stdout = new VariableValue();
        ce.stdout.type = VariableTypePb.STR;
        VariableValue varVal = tsr.variables.get("myTaskVar");
        String stdoutStr = "Got: " + varVal.strVal;

        // Useful for testing interrupts etc
        if (varVal.strVal.equals("SLEEP_LONG")) {
            Thread.sleep(1000 * 20);
        }

        if (varVal.strVal.equals("SLEEP_SHORT")) {
            System.out.println("Sleeping");
            Thread.sleep(1000 * 5);
            System.out.println("Done");
        }

        ce.stdout.strVal = stdoutStr;
        LHUtil.log(tsr.wfRunId, tsr.threadRunNumber, tsr.taskRunPosition, stdoutStr);

        Command cmd = new Command();
        cmd.type = CommandCase.TASK_RESULT_EVENT;
        cmd.taskResultEvent = ce;
        cmd.time = ce.time;

        prod.send(tsr.wfRunId, cmd, tsr.wfRunEventQueue);
        availThreadsSemaphore.release();
    }

    public static void doMain(LHConfig config) {
        // TestWorker worker = new TestWorker(config);
        // worker.run();
        TestWorkerGRPC worker = new TestWorkerGRPC(
            "hello",
            "localhost",
            5000,
            "task1",
            tsr -> {
                System.out.println("Hello from " + tsr.getWfRunId());
                return VariableValuePb
                    .newBuilder()
                    .setStr("Hello there")
                    .setType(VariableTypePb.STR)
                    .build();
            }
        );
        worker.start();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {}
        }
    }
}
