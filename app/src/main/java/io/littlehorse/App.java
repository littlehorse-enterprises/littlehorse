package io.littlehorse;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.KafkaStreamsServerImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.streams.state.HostInfo;

public class App {

    public static void doIdempotentSetup(LHConfig config)
        throws InterruptedException, ExecutionException {
        LHUtil.log("Creating topics!!");

        ArrayList<NewTopic> topics = new ArrayList<>();

        topics.add(
            new NewTopic(
                config.getCoreCmdTopicName(),
                config.getClusterPartitions(),
                config.getReplicationFactor()
            )
        );

        topics.add(
            new NewTopic(
                config.getTimerTopic(),
                config.getClusterPartitions(),
                config.getReplicationFactor()
            )
        );

        // Inputs to global state store's are always treated
        // as changelog topics. Therefore, we need it to be
        // compacted. In order to minimize restore time, we
        // also want the compaction to be quite aggressive.
        HashMap<String, String> globalMetaCLConfig = new HashMap<String, String>() {
            {
                put(TopicConfig.MAX_COMPACTION_LAG_MS_CONFIG, "5000");
                put(
                    TopicConfig.CLEANUP_POLICY_CONFIG,
                    TopicConfig.CLEANUP_POLICY_COMPACT
                );
            }
        };

        topics.add(
            new NewTopic(
                config.getGlobalMetadataCLTopicName(),
                1,
                config.getReplicationFactor()
            )
                .configs(globalMetaCLConfig)
        );
        for (NewTopic topic : topics) {
            config.createKafkaTopic(topic);
        }

        try {
            Thread.sleep(1000);
        } catch (Exception ignored) {}

        LHUtil.log("Done creating topics");
    }

    public static void main(String[] args)
        throws InterruptedException, ExecutionException, IOException {
        if (args.length == 1) {
            String arg = args[0];
            if (arg.equals("tester")) {
                tester();
                System.exit(0);
            }
        }

        LHConfig config = new LHConfig();
        doIdempotentSetup(config);
        KafkaStreamsServerImpl.doMain(config);
    }

    public static void tester() {
        // put whatever quick test code you want in here
        HostInfo test = new HostInfo("hello", 5000);
        System.out.println(test.toString());
    }
}
/*
// TODO: Find a better place for this documentation

Certain metadata objects (eg. WfSpec, TaskDef, ExternalEventDef) are "global"
in nature. This means three things:
1) Changes to them are low-throughput and infrequent
2) There are a relatively small number of them
3) Other resources (eg. WfRun) need to constantly consult them in order to run.

Items 1) and 2) imply that these metadata objects CAN be stored on one node.
Item 3) implies that we NEED every node to have a local copy of these
objects.

Furthermore, WfSpec's and TaskDef's depend on each other, and we want
strongly consistent processing (so that we can't accidentally delete a TaskDef
while processing a WfSpec that uses it, for example). Therefore, we want
all of the processing to be linearized, and therefore it needs to occur on
the same partition.

We localize all of the processing on one partition. However, we now need to
propagate that information to all server instances. We do that using a Kafka Streams
GlobalStore.

The GlobalStore listens to the "global-metadata-cl" topic. This file defines the
schema for the data in that topic.

*******
In the future, we're going to implement an RPC Task Queue so that Task Worker clients
do not need access to the Kafka brokers. This is quite a difficult task, and some
corner cases (eg. one client + multiple server instances + low-throughput task queue)
require the LH Servers to be able to communicate information about which instance
has how many pending tasks in what queue.

*******
We want to make the input topic a compacted topic in order to reduce rebalance time.
Therefore, we should key things by their full store keys.

We also want to reduce copying/deserializing if necessary. Therefore, we'll ensure that
the data input to the topic is just the raw bytes that gets stored.

*******
*/
