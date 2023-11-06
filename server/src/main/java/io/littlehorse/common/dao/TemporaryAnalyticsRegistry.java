package io.littlehorse.common.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.TaskMetricUpdateModel;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.WfMetricUpdateModel;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.store.LHKeyValueIterator;
import io.littlehorse.server.streams.store.RocksDBWrapper;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import lombok.extern.slf4j.Slf4j;

/**
 * TEMPORARY class. Will be re-thought and maybe deleted.
 */
@Slf4j
public class TemporaryAnalyticsRegistry {

    private Map<String, WfMetricUpdateModel> wfMetricPuts;
    private Map<String, TaskMetricUpdateModel> taskMetricPuts;
    private RocksDBWrapper rocksdb;
    private LHServerConfig config;
    private ProcessorContext<String, CommandProcessorOutput> ctx;

    public TemporaryAnalyticsRegistry(LHServerConfig config, ProcessorContext<String, CommandProcessorOutput> ctx, RocksDBWrapper rocksdb) {
        this.rocksdb = rocksdb;
        this.config = config;
        this.ctx = ctx;
        this.wfMetricPuts = new HashMap<>();
        this.taskMetricPuts = new HashMap<>();
    }

    public List<WfMetricUpdateModel> getWfMetricWindows(String wfSpecName, int wfSpecVersion, Date time) {
        List<WfMetricUpdateModel> out = new ArrayList<>();
        out.add(getWmUpdate(time, MetricsWindowLength.MINUTES_5, wfSpecName, wfSpecVersion));
        out.add(getWmUpdate(time, MetricsWindowLength.HOURS_2, wfSpecName, wfSpecVersion));
        out.add(getWmUpdate(time, MetricsWindowLength.DAYS_1, wfSpecName, wfSpecVersion));
        out.add(getWmUpdate(time, MetricsWindowLength.MINUTES_5, LHConstants.CLUSTER_LEVEL_METRIC, 0));
        out.add(getWmUpdate(time, MetricsWindowLength.HOURS_2, LHConstants.CLUSTER_LEVEL_METRIC, 0));
        out.add(getWmUpdate(time, MetricsWindowLength.DAYS_1, LHConstants.CLUSTER_LEVEL_METRIC, 0));
        return out;
    }

    private WfMetricUpdateModel getWmUpdate(
            Date windowStart, MetricsWindowLength type, String wfSpecName, int wfSpecVersion) {
        windowStart = LHUtil.getWindowStart(windowStart, type);
        String id = WfMetricUpdateModel.getObjectId(type, windowStart, wfSpecName, wfSpecVersion);
        if (wfMetricPuts.containsKey(id)) {
            return wfMetricPuts.get(id);
        }

        WfMetricUpdateModel out = rocksdb.get(id, WfMetricUpdateModel.class);
        if (out == null) {
            out = new WfMetricUpdateModel();
            out.windowStart = windowStart;
            out.type = type;
            out.wfSpecName = wfSpecName;
            out.wfSpecVersion = wfSpecVersion;
        }

        wfMetricPuts.put(id, out);
        return out;
    }

    public List<TaskMetricUpdateModel> getTaskMetricWindows(String taskDefName, Date time) {
        List<TaskMetricUpdateModel> out = new ArrayList<>();
        out.add(getTmUpdate(time, MetricsWindowLength.MINUTES_5, taskDefName));
        out.add(getTmUpdate(time, MetricsWindowLength.HOURS_2, taskDefName));
        out.add(getTmUpdate(time, MetricsWindowLength.DAYS_1, taskDefName));
        out.add(getTmUpdate(time, MetricsWindowLength.MINUTES_5, LHConstants.CLUSTER_LEVEL_METRIC));
        out.add(getTmUpdate(time, MetricsWindowLength.HOURS_2, LHConstants.CLUSTER_LEVEL_METRIC));
        out.add(getTmUpdate(time, MetricsWindowLength.DAYS_1, LHConstants.CLUSTER_LEVEL_METRIC));
        return out;
    }

    private TaskMetricUpdateModel getTmUpdate(Date windowStart, MetricsWindowLength type, String taskDefName) {
        windowStart = LHUtil.getWindowStart(windowStart, type);
        String id = TaskMetricUpdateModel.getStoreKey(type, windowStart, taskDefName);
        if (taskMetricPuts.containsKey(id)) {
            return taskMetricPuts.get(id);
        }

        TaskMetricUpdateModel out = rocksdb.get(id, TaskMetricUpdateModel.class);
        if (out == null) {
            out = new TaskMetricUpdateModel();
            out.windowStart = windowStart;
            out.type = type;
            out.taskDefName = taskDefName;
        }

        taskMetricPuts.put(id, out);
        return out;
    }

    public void commitCommand() {
        for (Map.Entry<String, TaskMetricUpdateModel> e : taskMetricPuts.entrySet()) {
            rocksdb.put(e.getValue());
        }
        for (Map.Entry<String, WfMetricUpdateModel> e : wfMetricPuts.entrySet()) {
            rocksdb.put(e.getValue());
        }
    }

    public void initCommand() {
        taskMetricPuts.clear();
        wfMetricPuts.clear();
    }

    public void forwardAndClearMetricsUpdatesUntil() {
        Map<String, TaskMetricUpdateModel> clusterTaskUpdates = new HashMap<>();

        try (LHKeyValueIterator<TaskMetricUpdateModel> iter = rocksdb.range("", "~", TaskMetricUpdateModel.class); ) {
            while (iter.hasNext()) {
                LHIterKeyValue<TaskMetricUpdateModel> next = iter.next();

                log.debug("Sending out metrics for {}", next.getKey());

                rocksdb.delete(next.getKey(), StoreableType.TASK_METRIC_UPDATE);
                TaskMetricUpdateModel tmu = next.getValue();
                forwardTaskMetricUpdate(tmu);

                // Update the cluster-level metrics
                String clusterTaskUpdateKey =
                        TaskMetricUpdateModel.getStoreKey(tmu.type, tmu.windowStart, LHConstants.CLUSTER_LEVEL_METRIC);
                TaskMetricUpdateModel clusterTaskUpdate;
                if (clusterTaskUpdates.containsKey(clusterTaskUpdateKey)) {
                    clusterTaskUpdate = clusterTaskUpdates.get(clusterTaskUpdateKey);
                } else {
                    clusterTaskUpdate =
                            new TaskMetricUpdateModel(tmu.windowStart, tmu.type, LHConstants.CLUSTER_LEVEL_METRIC);
                }

                clusterTaskUpdate.merge(tmu);
                clusterTaskUpdates.put(clusterTaskUpdateKey, clusterTaskUpdate);
                rocksdb.delete(tmu);
            }
        }

        // Forward the cluster level task updates
        for (TaskMetricUpdateModel tmu : clusterTaskUpdates.values()) {
            forwardTaskMetricUpdate(tmu);
        }

        // get ready to update the cluster level WF Metrics
        Map<String, WfMetricUpdateModel> clusterWfUpdates = new HashMap<>();

        try (LHKeyValueIterator<WfMetricUpdateModel> iter = rocksdb.range("", "~", WfMetricUpdateModel.class); ) {
            while (iter.hasNext()) {
                LHIterKeyValue<WfMetricUpdateModel> next = iter.next();
                WfMetricUpdateModel wmu = next.getValue();
                forwardWfMetricUpdate(wmu);

                // Update the cluster-level metrics
                String clusterWfUpdateKey =
                        WfMetricUpdateModel.getStoreKey(wmu.type, wmu.windowStart, LHConstants.CLUSTER_LEVEL_METRIC, 0);
                WfMetricUpdateModel clusterWfUpdate;
                if (clusterWfUpdates.containsKey(clusterWfUpdateKey)) {
                    clusterWfUpdate = clusterWfUpdates.get(clusterWfUpdateKey);
                } else {
                    clusterWfUpdate =
                            new WfMetricUpdateModel(wmu.windowStart, wmu.type, LHConstants.CLUSTER_LEVEL_METRIC, 0);
                }
                clusterWfUpdate.merge(wmu);
                clusterWfUpdates.put(clusterWfUpdateKey, clusterWfUpdate);

                rocksdb.delete(wmu);
            }
        }

        // Forward the cluster level task updates
        for (WfMetricUpdateModel wmu : clusterWfUpdates.values()) {
            forwardWfMetricUpdate(wmu);
        }
    }

    private void forwardTaskMetricUpdate(TaskMetricUpdateModel tmu) {
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = tmu.getPartitionKey();
        cpo.topic = config.getRepartitionTopicName();
        cpo.payload = new RepartitionCommand(tmu, new Date(), tmu.getPartitionKey());
        Record<String, CommandProcessorOutput> out =
                new Record<>(tmu.getPartitionKey(), cpo, System.currentTimeMillis());
        ctx.forward(out);
    }

    private void forwardWfMetricUpdate(WfMetricUpdateModel wmu) {
        CommandProcessorOutput cpo = new CommandProcessorOutput();
        cpo.partitionKey = wmu.getPartitionKey();
        cpo.topic = config.getRepartitionTopicName();
        cpo.payload = new RepartitionCommand(wmu, new Date(), wmu.getPartitionKey());
        Record<String, CommandProcessorOutput> out =
                new Record<>(wmu.getPartitionKey(), cpo, System.currentTimeMillis());
        ctx.forward(out);
    }

    public void onWfStart(String wfSpecName, int wfSpecVersion) {
        for (WfMetricUpdateModel wmu : getWfMetricWindows(wfSpecName, wfSpecVersion, new Date())) {
            wmu.numEntries++;
            wmu.totalStarted++;
        }
    }

    public void onTaskScheduled(String taskDefName) {
        for (TaskMetricUpdateModel tmu : getTaskMetricWindows(taskDefName, new Date())) {
            tmu.numEntries++;
            tmu.totalScheduled++;
        }
    }

    public void onTaskStarted(String taskDefName, Date scheduledTime) {
        for (TaskMetricUpdateModel tmu : getTaskMetricWindows(taskDefName, new Date())) {
            tmu.numEntries++;
            tmu.totalStarted++;
            long scheduleToStart = System.currentTimeMillis() - scheduledTime.getTime();
            tmu.scheduleToStartTotal += scheduleToStart;
            if (scheduleToStart > tmu.scheduleToStartMax) {
                tmu.scheduleToStartMax = scheduleToStart;
            }
        }
    }

    public void onTaskCompleted(String taskDefName, Date startedTime) {
        for (TaskMetricUpdateModel tmu : getTaskMetricWindows(taskDefName, new Date())) {
            tmu.numEntries++;
            tmu.totalStarted++;
            long startToComplete = System.currentTimeMillis() - startedTime.getTime();
            tmu.startToCompleteTotal += startToComplete;
            if (startToComplete > tmu.startToCompleteMax) {
                tmu.startToCompleteMax = startToComplete;
            }
        }
    }

    public void onWfCompleted(String wfSpecName, int wfSpecVersion, Date startedTime) {
        for (WfMetricUpdateModel wmu : getWfMetricWindows(wfSpecName, wfSpecVersion, new Date())) {
            wmu.numEntries++;
            wmu.totalCompleted++;
            long startToComplete = System.currentTimeMillis() - startedTime.getTime();
            wmu.startToCompleteTotal += startToComplete;
            if (startToComplete > wmu.startToCompleteMax) {
                wmu.startToCompleteMax = startToComplete;
            }
        }
    }
}
