package io.littlehorse;

import io.littlehorse.common.LHConfig;
import io.littlehorse.server.KafkaStreamsServerImpl;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;

@Slf4j
public class App {

    public static void doIdempotentSetup(LHConfig config)
        throws InterruptedException, ExecutionException {
        log.info("Creating topics!!");

        for (NewTopic topic : config.getAllTopics()) {
            config.createKafkaTopic(topic);
        }

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ex) {
            log.warn("InterruptedException was ignored");
        }

        log.info("Done creating topics");
    }

    public static void main(String[] args)
        throws IOException, InterruptedException, ExecutionException {
        LHConfig config;

        if (args.length > 0) {
            log.info("Loading configuration from file '{}'", args[0]);
            config = new LHConfig(args[0]);
        } else {
            log.info("Loading configuration from env variables");
            config = new LHConfig();
        }

        log.info("Settings:\n{}", config);

        if (config.shouldCreateTopics()) {
            doIdempotentSetup(config);
        }

        KafkaStreamsServerImpl.doMain(config);
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
