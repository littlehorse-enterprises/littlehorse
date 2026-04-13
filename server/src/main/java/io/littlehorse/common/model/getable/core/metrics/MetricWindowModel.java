package io.littlehorse.common.model.getable.core.metrics;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.getable.objectId.MetricWindowIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.CountAndTiming;
import io.littlehorse.sdk.common.proto.MetricWindow;
import io.littlehorse.sdk.common.proto.MetricWindowType;
import io.littlehorse.sdk.common.proto.TaskMetrics;
import io.littlehorse.sdk.common.proto.WfMetrics;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
@Setter
@NoArgsConstructor
public class MetricWindowModel extends CoreGetable<MetricWindow> {

    public static final String STARTED = "started";
    public static final String RUNNING_TO_COMPLETED = "running_to_completed";
    public static final String RUNNING_TO_ERROR = "running_to_error";
    public static final String RUNNING_TO_EXCEPTION = "running_to_exception";
    public static final String RUNNING_TO_HALTING = "running_to_halting";
    public static final String RUNNING_TO_HALTED = "running_to_halted";
    public static final String HALTING_TO_HALTED = "halting_to_halted";
    public static final String HALTED_TO_RUNNING = "halted_to_running";

    public static final String TASKRUN_CREATED_TO_COMPLETED = "taskrun_created_to_completed";
    public static final String TASKRUN_CREATED_TO_ERROR = "taskrun_created_to_error";
    public static final String TASKRUN_CREATED_TO_EXCEPTION = "taskrun_created_to_exception";
    public static final String TASKATTEMPT_PENDING_TO_SCHEDULED = "taskattempt_pending_to_scheduled";
    public static final String TASKATTEMPT_SCHEDULED_TO_RUNNING = "taskattempt_scheduled_to_running";
    public static final String TASKATTEMPT_RUNNING_TO_ERROR = "taskattempt_running_to_error";
    public static final String TASKATTEMPT_RUNNING_TO_SUCCESS = "taskattempt_running_to_success";
    public static final String TASKATTEMPT_RUNNING_TO_EXCEPTION = "taskattempt_running_to_exception";

    private MetricWindowIdModel id;
    private Map<String, CountAndTimingModel> metrics;

    public MetricWindowModel(MetricWindowIdModel id, Map<String, CountAndTimingModel> metrics) {
        this.id = id;
        this.metrics = metrics;
    }

    public void mergeFrom(Map<String, CountAndTimingModel> otherMetrics) {
        for (Entry<String, CountAndTimingModel> entry : otherMetrics.entrySet()) {
            String key = entry.getKey();
            CountAndTimingModel incoming = entry.getValue();
            CountAndTimingModel existing = metrics.get(key);
            if (existing == null) {
                metrics.put(key, incoming);
            } else {
                existing.mergeFrom(incoming);
            }
        }
    }

    @Override
    public Class<MetricWindow> getProtoBaseClass() {
        return MetricWindow.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        MetricWindow p = (MetricWindow) proto;
        id = LHSerializable.fromProto(p.getId(), MetricWindowIdModel.class, context);
        metrics = new HashMap<>();

        if (p.hasWorkflow()) {
            WfMetrics wfMetrics = p.getWorkflow();
            addMetricIfPresent(metrics, STARTED, wfMetrics.hasStarted(), wfMetrics.getStarted(), context);
            addMetricIfPresent(
                    metrics,
                    RUNNING_TO_COMPLETED,
                    wfMetrics.hasRunningToCompleted(),
                    wfMetrics.getRunningToCompleted(),
                    context);
            addMetricIfPresent(
                    metrics, RUNNING_TO_ERROR, wfMetrics.hasRunningToError(), wfMetrics.getRunningToError(), context);
            addMetricIfPresent(
                    metrics,
                    RUNNING_TO_EXCEPTION,
                    wfMetrics.hasRunningToException(),
                    wfMetrics.getRunningToException(),
                    context);
            addMetricIfPresent(
                    metrics,
                    RUNNING_TO_HALTING,
                    wfMetrics.hasRunningToHalting(),
                    wfMetrics.getRunningToHalting(),
                    context);
            addMetricIfPresent(
                    metrics,
                    RUNNING_TO_HALTED,
                    wfMetrics.hasRunningToHalted(),
                    wfMetrics.getRunningToHalted(),
                    context);

            addMetricIfPresent(
                    metrics,
                    HALTING_TO_HALTED,
                    wfMetrics.hasHaltingToHalted(),
                    wfMetrics.getHaltingToHalted(),
                    context);
            addMetricIfPresent(
                    metrics,
                    HALTED_TO_RUNNING,
                    wfMetrics.hasHaltedToRunning(),
                    wfMetrics.getHaltedToRunning(),
                    context);
            return;
        }

        if (p.hasTask()) {
            TaskMetrics taskMetrics = p.getTask();
            addMetricIfPresent(
                    metrics,
                    TASKRUN_CREATED_TO_COMPLETED,
                    taskMetrics.hasTaskrunCreatedToCompleted(),
                    taskMetrics.getTaskrunCreatedToCompleted(),
                    context);
            addMetricIfPresent(
                    metrics,
                    TASKRUN_CREATED_TO_ERROR,
                    taskMetrics.hasTaskrunCreatedToError(),
                    taskMetrics.getTaskrunCreatedToError(),
                    context);
            addMetricIfPresent(
                    metrics,
                    TASKRUN_CREATED_TO_EXCEPTION,
                    taskMetrics.hasTaskrunCreatedToException(),
                    taskMetrics.getTaskrunCreatedToException(),
                    context);
            addMetricIfPresent(
                    metrics,
                    TASKATTEMPT_PENDING_TO_SCHEDULED,
                    taskMetrics.hasTaskattemptPendingToScheduled(),
                    taskMetrics.getTaskattemptPendingToScheduled(),
                    context);
            addMetricIfPresent(
                    metrics,
                    TASKATTEMPT_SCHEDULED_TO_RUNNING,
                    taskMetrics.hasTaskattemptScheduledToRunning(),
                    taskMetrics.getTaskattemptScheduledToRunning(),
                    context);
            addMetricIfPresent(
                    metrics,
                    TASKATTEMPT_RUNNING_TO_ERROR,
                    taskMetrics.hasTaskattemptRunningToError(),
                    taskMetrics.getTaskattemptRunningToError(),
                    context);
            addMetricIfPresent(
                    metrics,
                    TASKATTEMPT_RUNNING_TO_SUCCESS,
                    taskMetrics.hasTaskattemptRunningToSuccess(),
                    taskMetrics.getTaskattemptRunningToSuccess(),
                    context);
            addMetricIfPresent(
                    metrics,
                    TASKATTEMPT_RUNNING_TO_EXCEPTION,
                    taskMetrics.hasTaskattemptRunningToException(),
                    taskMetrics.getTaskattemptRunningToException(),
                    context);
        }
    }

    @Override
    public MetricWindow.Builder toProto() {
        MetricWindow.Builder out = MetricWindow.newBuilder().setId(id.toProto());

        if (id.getMetricType() == MetricWindowType.WORKFLOW_METRIC) {
            WfMetrics.Builder wfMetrics = WfMetrics.newBuilder();
            putMetricIfPresent(wfMetrics::setStarted, STARTED);
            putMetricIfPresent(wfMetrics::setRunningToCompleted, RUNNING_TO_COMPLETED);
            putMetricIfPresent(wfMetrics::setRunningToError, RUNNING_TO_ERROR);
            putMetricIfPresent(wfMetrics::setRunningToException, RUNNING_TO_EXCEPTION);
            putMetricIfPresent(wfMetrics::setRunningToHalting, RUNNING_TO_HALTING);
            putMetricIfPresent(wfMetrics::setRunningToHalted, RUNNING_TO_HALTED);
            putMetricIfPresent(wfMetrics::setHaltingToHalted, HALTING_TO_HALTED);
            putMetricIfPresent(wfMetrics::setHaltedToRunning, HALTED_TO_RUNNING);
            out.setWorkflow(wfMetrics);
        } else if (id.getMetricType() == MetricWindowType.TASK_METRIC) {
            TaskMetrics.Builder taskMetrics = TaskMetrics.newBuilder();
            putMetricIfPresent(taskMetrics::setTaskrunCreatedToCompleted, TASKRUN_CREATED_TO_COMPLETED);
            putMetricIfPresent(taskMetrics::setTaskrunCreatedToError, TASKRUN_CREATED_TO_ERROR);
            putMetricIfPresent(taskMetrics::setTaskrunCreatedToException, TASKRUN_CREATED_TO_EXCEPTION);
            putMetricIfPresent(taskMetrics::setTaskattemptPendingToScheduled, TASKATTEMPT_PENDING_TO_SCHEDULED);
            putMetricIfPresent(taskMetrics::setTaskattemptScheduledToRunning, TASKATTEMPT_SCHEDULED_TO_RUNNING);
            putMetricIfPresent(taskMetrics::setTaskattemptRunningToError, TASKATTEMPT_RUNNING_TO_ERROR);
            putMetricIfPresent(taskMetrics::setTaskattemptRunningToSuccess, TASKATTEMPT_RUNNING_TO_SUCCESS);
            putMetricIfPresent(taskMetrics::setTaskattemptRunningToException, TASKATTEMPT_RUNNING_TO_EXCEPTION);
            out.setTask(taskMetrics);
        }

        return out;
    }

    private void putMetricIfPresent(Consumer<CountAndTiming> setter, String key) {
        CountAndTimingModel model = metrics.get(key);
        if (model != null) {
            setter.accept(model.toProto().build());
        } else {
            setter.accept(CountAndTiming.getDefaultInstance());
        }
    }

    private static void addMetricIfPresent(
            Map<String, CountAndTimingModel> metrics,
            String key,
            boolean hasMetric,
            CountAndTiming metric,
            ExecutionContext context) {
        if (!hasMetric) {
            return;
        }
        metrics.put(key, LHSerializable.fromProto(metric, CountAndTimingModel.class, context));
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        GetableIndex<MetricWindowModel> allWfSpecByDate = new GetableIndex<>(
                List.of(Pair.of("wfSpecName", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageType.LOCAL),
                model -> model.id.getWfSpecId() != null);
        return List.of(allWfSpecByDate);
    }

    @Override
    public List<IndexedField> getIndexValues(String attributeName, Optional<TagStorageType> tagStorageType) {
        if ("wfSpecName".equals(attributeName)) {
            return List.of(new IndexedField(attributeName, id.getWfSpecId().getName(), tagStorageType.get()));
        }
        return List.of();
    }

    @Override
    public Date getCreatedAt() {
        return id.getWindowStart();
    }

    @Override
    public MetricWindowIdModel getObjectId() {
        return id;
    }
}
