package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.OutputTopicConfig;
import io.littlehorse.sdk.common.proto.OutputTopicRecord;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.wfsdk.NodeOutput;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.wfsdk.internal.WorkflowImpl;
import io.littlehorse.sdk.worker.LHTaskWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;
/*
 * This is a simple example, which does two things:
 * 1. Declare an "input-name" variable of type String
 * 2. Pass that variable into the execution of the "greet" task.
 */
public class OutputTopicExample {

    public static Workflow getWorkflow() {
        return new WorkflowImpl(
            "output-topic",
            wf -> {
                WfRunVariable theName = wf.addVariable("input-name", VariableType.STR).searchable().asPublic();
                WfRunVariable ignoredGreeting = wf.declareStr("ignored");
                WfRunVariable publicGreeting = wf.declareStr("public-greeting").asPublic();

                NodeOutput result = wf.execute("greet", theName);

                ignoredGreeting.assign(result);
                publicGreeting.assign(result);
            }
        );
    }

    public static Properties getConfigProps() throws IOException {
        Properties props = new Properties();
        File configPath = Path.of(
            System.getProperty("user.home"),
            ".config/littlehorse.config"
        ).toFile();
        if(configPath.exists()){
            props.load(new FileInputStream(configPath));
        }
        return props;
    }

    public static LHTaskWorker getTaskWorker(LHConfig config) {
        MyWorker executable = new MyWorker();
        LHTaskWorker worker = new LHTaskWorker(executable, "greet", config);

        // Gracefully shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(worker::close));
        return worker;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Let's prepare the configurations
        Properties props = getConfigProps();
        LHConfig config = new LHConfig(props);

        LittleHorseBlockingStub client = config.getBlockingStub();
        client.putTenant(PutTenantRequest.newBuilder().setId("default").setOutputTopicConfig(OutputTopicConfig.newBuilder()).build());

        // Allow output topic creation
        Thread.sleep(5000);

        // Start worker
        LHTaskWorker worker = getTaskWorker(config);
        worker.registerTaskDef();
        worker.start();

        // Register WfSpec
        getWorkflow().registerWfSpec(config.getBlockingStub());

        // Start Kafka Consumer
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", "localhost:9092");
        kafkaProps.put("group.id", "obiwan");
        kafkaProps.put("auto.offset.reset", "earliest");
        kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OutputTopicRecordDeserializer.class);
        kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass());

        KafkaConsumer<String, OutputTopicRecord> consumer = new KafkaConsumer<>(kafkaProps);

        String outputTopicName = "my-cluster--default--execution";
        String metadataOutputTopicName = "my-cluster--default--metadata";
        consumer.subscribe(List.of(outputTopicName, metadataOutputTopicName));

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::close));
        while (true) {
            ConsumerRecords<String, OutputTopicRecord> records = consumer.poll(Duration.ofSeconds(5));
            for (ConsumerRecord<String, OutputTopicRecord> record : records) {
                System.out.println("****** " + record.topic() + "*******");
                System.out.println(LHLibUtil.protoToJson(record.value()));
            }
        }
    }
}
