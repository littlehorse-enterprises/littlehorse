package io.littlehorse.server.streams;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.common.LHServerConfig;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.streams.taskqueue.TaskQueueManager;
import io.littlehorse.server.streams.util.AsyncWaiters;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyDescription;
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

    @Test
    public void shouldBeCompatibleWithV1Topology() {
        TopologyDescription topology = serverTopology.build().describe();
        TopologyDescription topologyV1 = ServerTopology.initCoreTopology(
                        config, server, metadataCache, globalTaskQueueManager, asyncWaiters)
                .describe();
        assertThat(topology.subtopologies()).hasSameSizeAs(topologyV1.subtopologies());
        TopologyDescription.Subtopology[] subtopologies =
                topology.subtopologies().toArray(new TopologyDescription.Subtopology[] {});
        TopologyDescription.Subtopology[] subtopologiesV1 =
                topologyV1.subtopologies().toArray(new TopologyDescription.Subtopology[] {});
        for (int i = 0; i < subtopologies.length; i++) {
            TopologyDescription.Subtopology subtopology = subtopologies[i];
            TopologyDescription.Subtopology subtopologyV1 = subtopologiesV1[i];
            TopologyDescription.Node firstNodeTopology =
                    subtopology.nodes().toArray(new TopologyDescription.Node[] {})[0];
            TopologyDescription.Node firstNodeV1Topology =
                    subtopologyV1.nodes().toArray(new TopologyDescription.Node[] {})[0];
            assertThat(firstNodeTopology.name()).isEqualTo(firstNodeV1Topology.name());

            Set<String> storesTopology = subtopology.nodes().stream()
                    .filter(node -> node instanceof TopologyDescription.Processor)
                    .map(node -> (TopologyDescription.Processor) node)
                    .flatMap(processor -> processor.stores().stream())
                    .collect(Collectors.toSet());
            Set<String> storesTopologyV1 = subtopologyV1.nodes().stream()
                    .filter(node -> node instanceof TopologyDescription.Processor)
                    .map(node -> (TopologyDescription.Processor) node)
                    .flatMap(processor -> processor.stores().stream())
                    .collect(Collectors.toSet());

            assertThat(storesTopology).containsAll(storesTopologyV1);
        }
    }
}
