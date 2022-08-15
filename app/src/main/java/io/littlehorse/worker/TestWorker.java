package io.littlehorse.worker;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.utils.Bytes;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.event.TaskResultEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.proto.TaskResultCodePb;
import io.littlehorse.common.proto.scheduler.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;

/**
 * This is a shortcut, obviously.
 */
public class TestWorker {
    private KafkaConsumer<String, Bytes> cons;
    private LHProducer prod;
    private LHProducer txnProd;
    private ExecutorService threadPool;
    private LHConfig config;

    private List<TaskScheduleRequest> acknowledgedTasks;
    private Map<TopicPartition, OffsetAndMetadata> offsetMap;

    public TestWorker(LHConfig config) {
        this.prod = config.getProducer();
        this.txnProd = config.getTxnProducer();
        this.cons = config.getKafkaConsumer(Arrays.asList(
            System.getenv().getOrDefault("LHORSE_TASK_DEF_ID", "task1")
        ));
        this.config = config;
        this.threadPool = Executors.newFixedThreadPool(32);
        acknowledgedTasks = new ArrayList<>();
        offsetMap = new HashMap<>();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.prod.close();
            this.cons.close();
            this.threadPool.shutdown();
        }));
    }

    public void run() {
        while(true) {
            ConsumerRecords<String, Bytes> records = cons.poll(
                Duration.ofMillis(50));

            acknowledgedTasks.clear();
            offsetMap.clear();
            txnProd.beginTransaction();
            try {
                records.forEach(this::acknowledgeRequest);
                txnProd.sendOffsetsToTransaction(offsetMap, cons.groupMetadata());
                txnProd.commitTransaction();
            } catch(Exception exn) {
                txnProd.abortTransaction();
            }

            enqueueAcknowledgedTasks();
        }
    }

    private void acknowledgeRequest(ConsumerRecord<String, Bytes> r) {
        TaskScheduleRequest tsr;
        try {
            tsr = LHSerializable.fromBytes(
                r.value().get(), TaskScheduleRequest.class, config
            );
        } catch(LHSerdeError exn) {
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

        txnProd.send(tsr.wfRunId, event, tsr.replyKafkaTopic);
        acknowledgedTasks.add(tsr);

        TopicPartition partition = new TopicPartition(r.topic(), r.partition());
        OffsetAndMetadata offset = new OffsetAndMetadata(r.offset() + 1);
        offsetMap.put(partition, offset);
    }

    private void enqueueAcknowledgedTasks() {
        for (TaskScheduleRequest tsr: acknowledgedTasks) {
            this.threadPool.submit(() -> {this.executeTask(tsr);});
        }
    }

    private void executeTask(TaskScheduleRequest tsr) {
        TaskResultEvent ce = new TaskResultEvent();
        ce.taskRunNumber = tsr.taskRunNumber;
        ce.taskRunPosition = tsr.taskRunPosition;
        ce.threadRunNumber = tsr.threadRunNumber;
        ce.time = new Date();

        ce.resultCode = TaskResultCodePb.SUCCESS;
        ce.stderr = null;
        ce.stdout = ("Completed task " + tsr.taskDefName + " " + tsr.wfRunId).getBytes();

        WfRunEvent event = new WfRunEvent();
        event.wfRunId = tsr.wfRunId;
        event.wfSpecId = tsr.wfSpecId;
        event.time = ce.time;
        event.taskResult = ce;
        event.type = EventCase.TASK_RESULT;

        LHUtil.log("Starting " + tsr.wfRunId);
        try {Thread.sleep(5000);} catch(Exception exn) {}
        LHUtil.log("Completing " + tsr.wfRunId);
        prod.send(tsr.wfRunId, event, tsr.replyKafkaTopic);
    }

    public static void doMain(LHConfig config) {
        TestWorker worker = new TestWorker(config);
        worker.run();
    }
}
