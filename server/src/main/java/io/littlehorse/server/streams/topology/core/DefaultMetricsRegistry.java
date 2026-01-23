package io.littlehorse.server.streams.topology.core;

import io.littlehorse.sdk.common.proto.AggregationType;
import io.littlehorse.sdk.common.proto.MetricEntityType;
import io.littlehorse.sdk.common.proto.MetricRecordingLevel;
import io.littlehorse.sdk.common.proto.MetricScope;
import io.littlehorse.sdk.common.proto.MetricSpec;
import io.littlehorse.sdk.common.proto.StatusTransition;
import com.google.protobuf.Duration;
import java.util.List;

public class DefaultMetricsRegistry {

    public static List<MetricSpec> builtIn() {
        return List.of(
            // Global workflow completion count
            MetricSpec.newBuilder()
                .setId("global-workflow-completed-5m")
                .setAggregationType(AggregationType.COUNT)
                .setScope(MetricScope.newBuilder().setGlobal(true).build())
                .setTransition(StatusTransition.newBuilder()
                    .setEntity(MetricEntityType.METRIC_WF_RUN)
                    .setFromStatus("RUNNING")
                    .setToStatus("COMPLETED")
                    .build())
                .setWindowLength(Duration.newBuilder().setSeconds(300).build())
                .build(),

            // Global workflow failure count
            MetricSpec.newBuilder()
                .setId("global-workflow-failed-5m")
                .setAggregationType(AggregationType.COUNT)
                .setScope(MetricScope.newBuilder().setGlobal(true).build())
                .setTransition(StatusTransition.newBuilder()
                    .setEntity(MetricEntityType.METRIC_WF_RUN)
                    .setFromStatus("RUNNING")
                    .setToStatus("ERROR")
                    .build())
                .setWindowLength(Duration.newBuilder().setSeconds(300).build())
                .build(),

            // Global task execution latency
            MetricSpec.newBuilder()
                .setId("global-task-execution-latency-5m")
                .setAggregationType(AggregationType.LATENCY)
                .setScope(MetricScope.newBuilder().setGlobal(true).build())
                .setTransition(StatusTransition.newBuilder()
                    .setEntity(MetricEntityType.METRIC_TASK_RUN)
                    .setFromStatus("TASK_RUNNING")
                    .setToStatus("TASK_SUCCESS")
                    .build())
                .setWindowLength(Duration.newBuilder().setSeconds(300).build())
                .build()

            // Add more as needed
        );
    }

    public static MetricRecordingLevel getDefaultRecordingLevel() {
        return MetricRecordingLevel.INFO;
    }
}