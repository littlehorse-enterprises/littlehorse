package io.littlehorse.worker;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskResultEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.proto.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
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

    public TestWorker(LHConfig config) {
        this.prod = config.getProducer();
        this.txnProd = config.getTxnProducer();
        this.cons =
            config.getKafkaConsumer(
                Arrays.asList(
                    System.getenv().getOrDefault("LHORSE_TASK_DEF_ID", "task1")
                )
            );
        this.config = config;
        this.threadPool = Executors.newFixedThreadPool(config.getWorkerThreads());
        acknowledgedTasks = new ArrayList<>();
        offsetMap = new HashMap<>();
        availThreadsSemaphore = new Semaphore(config.getWorkerThreads());

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

        TaskStartedEvent se = new TaskStartedEvent();
        se.taskRunNumber = tsr.taskRunNumber;
        se.taskRunPosition = tsr.taskRunPosition;
        se.threadRunNumber = tsr.threadRunNumber;
        se.time = new Date();

        WfRunEvent event = new WfRunEvent();
        event.wfRunId = tsr.wfRunId;
        event.wfSpecId = tsr.wfSpecId;
        event.time = se.time;
        event.startedEvent = se;
        event.type = EventCase.STARTED_EVENT;

        txnProd.send(tsr.wfRunId, event, tsr.wfRunEventQueue);
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

        ce.stdout.strVal = stdoutStr;
        LHUtil.log(tsr.wfRunId, tsr.taskRunPosition, stdoutStr);

        WfRunEvent event = new WfRunEvent();
        event.wfRunId = tsr.wfRunId;
        event.wfSpecId = tsr.wfSpecId;
        event.time = ce.time;
        event.taskResult = ce;
        event.type = EventCase.TASK_RESULT;

        prod.send(tsr.wfRunId, event, tsr.wfRunEventQueue).get();
        availThreadsSemaphore.release();
    }

    public static void doMain(LHConfig config) {
        TestWorker worker = new TestWorker(config);
        worker.run();
    }
}
