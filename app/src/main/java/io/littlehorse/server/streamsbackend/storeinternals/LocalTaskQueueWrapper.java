package io.littlehorse.server.streamsbackend.storeinternals;

import io.littlehorse.common.LHConfig;
import io.littlehorse.server.streamsbackend.ServerTopology;
import io.littlehorse.server.streamsbackend.storeinternals.index.Tag;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHIterKeyValue;
import io.littlehorse.server.streamsbackend.storeinternals.utils.LHKeyValueIterator;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;

public class LocalTaskQueueWrapper {

    private KafkaStreams streams;
    private LHConfig config;
    private InflightList inflightList;

    public LocalTaskQueueWrapper(
        KafkaStreams streams,
        LHConfig config,
        InflightList inflightList
    ) {
        this.streams = streams;
        this.config = config;
        this.inflightList = inflightList;
    }

    // Future optimization: reuse the same iterator for multiple concurrent
    // calls to pollTask().
    public String pollTask(String taskDefName) {
        LHROStoreWrapper store = new LHROStoreWrapper(
            streams.store(
                StoreQueryParameters.fromNameAndType(
                    ServerTopology.CORE_STORE,
                    QueryableStoreTypes.keyValueStore()
                )
            ),
            config
        );
        // try (
        //     LHKeyValueIterator<Tag> tagIter = store.prefixScan(
        //         Tag.getTagPrefixForPendingTasks(taskDefName),
        //         Tag.class
        //     )
        // ) {
        //     while (tagIter.hasNext()) {
        //         LHIterKeyValue<Tag> next = tagIter.next();
        //         Tag tag = next.getValue();
        //         String taskRunId = tag.describedObjectId;
        //         if (inflightList.markInFlight(taskDefName, taskRunId)) {
        //             return tag.describedObjectId;
        //         }
        //     }
        // }
        return null;
    }
}
