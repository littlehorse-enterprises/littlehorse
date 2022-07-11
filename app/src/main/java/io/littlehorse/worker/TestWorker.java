package io.littlehorse.worker;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.event.TaskCompletedEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.event.TaskStartedEvent;
import io.littlehorse.common.model.event.WFRunEvent;
import io.littlehorse.common.proto.WFRunEventPb.EventCase;
import io.littlehorse.common.serde.TaskScheduleRequestDeserializer;
import io.littlehorse.common.serde.WFRunEventSerializer;

/**
 * This is a shortcut, obviously.
 */
public class TestWorker {
    private KafkaConsumer<String, TaskScheduleRequest> cons;
    private KafkaProducer<String, WFRunEvent> prod;
    private ExecutorService threadPool;

    public TestWorker(LHConfig config) {
        this.cons = config.getKafkaConsumer(
            TaskScheduleRequestDeserializer.class
        );
        this.prod = config.getKafkaProducer(WFRunEventSerializer.class);
        this.cons.subscribe(Pattern.compile("task*"));
        this.threadPool = Executors.newFixedThreadPool(32);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.prod.close();
            this.cons.close();
            this.threadPool.shutdown();
        }));
    }

    public void run() {
        while(true) {
            ConsumerRecords<String, TaskScheduleRequest> records = cons.poll(
                Duration.ofMillis(50));
            records.forEach(this::processRequest);
        }
    }

    private void processRequest(ConsumerRecord<String, TaskScheduleRequest> r) {
        System.out.println("Processing for " + r.key() + " part " + r.hashCode());
        TaskScheduleRequest tsr = r.value();

        TaskStartedEvent se = new TaskStartedEvent();
        se.taskRunNumber = tsr.taskRunNumber;
        se.taskRunPosition = tsr.taskRunPosition;
        se.threadRunNumber = tsr.threadRunNumber;
        se.time = new Date();

        WFRunEvent event = new WFRunEvent();
        event.wfRunId = tsr.wfRunId;
        event.wfSpecId = tsr.wfSpecId;
        event.time = se.time;
        event.startedEvent = se;
        event.type = EventCase.STARTED_EVENT;

        prod.send(new ProducerRecord<String,WFRunEvent>(
            tsr.replyKafkaTopic, tsr.wfRunId, event
        ));

        this.threadPool.submit(() -> { this.herdCats(tsr);});
    }

    private void herdCats(TaskScheduleRequest tsr) {
        TaskCompletedEvent ce = new TaskCompletedEvent();
        ce.taskRunNumber = tsr.taskRunNumber;
        ce.taskRunPosition = tsr.taskRunPosition;
        ce.threadRunNumber = tsr.threadRunNumber;
        ce.time = new Date();

        ce.success = true;
        ce.stderr = null;
        ce.stdout = ("Completed task " + tsr.taskDefName + " " + tsr.wfRunId)
            .getBytes();

        WFRunEvent event = new WFRunEvent();
        event.wfRunId = tsr.wfRunId;
        event.wfSpecId = tsr.wfSpecId;
        event.time = ce.time;
        event.completedEvent = ce;
        event.type = EventCase.COMPLETED_EVENT;

        System.out.println("Completing " + tsr.wfRunId);
        prod.send(new ProducerRecord<String, WFRunEvent>(
            tsr.replyKafkaTopic, tsr.wfRunId, event
        ));
    }

    public static void doMain(LHConfig config) {
        TestWorker worker = new TestWorker(config);
        worker.run();
    }
}
