package io.littlehorse.server.streams;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServerTopologyV2Test {

    private final LHServerConfig config = mock();
    private final LHServer server = mock();
    private final MetadataCache metadataCache = mock();
    private final TaskQueueManager globalTaskQueueManager = mock();
    private final AsyncWaiters asyncWaiters = mock();

    private ServerTopologyV2 serverTopology;

    @BeforeEach
    public void setup() {
        when(config.getCoreCmdTopicName()).thenReturn("core-cmd-topic");
        when(config.getRepartitionTopicName()).thenReturn("repartition-topic");
        when(config.getMetadataCmdTopicName()).thenReturn("metadata-topic");
        serverTopology = new ServerTopologyV2(config, server, metadataCache, globalTaskQueueManager, asyncWaiters);
    }

    @Test
    public void shouldCreateValidTopology() {
        Topology topology = serverTopology.build();
        TopologyTestDriver driver = new TopologyTestDriver(topology);
        driver.close();
        System.out.println(topology.describe().toString());
    }
}
