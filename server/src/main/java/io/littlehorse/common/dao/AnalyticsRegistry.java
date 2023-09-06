package io.littlehorse.common.dao;

public interface AnalyticsRegistry {

    // TODO: add some methods which allow registering the necessary analytics such
    // as task started, task completed, wfrun failed, etc.
}

// below is some half-working old code. Keep these comments here for now; we
// will
// delete the comments after we re-implement Analytics. But this code is a good
// starter point for how to write the AnalyticsRegistryImpl.

// public List<WfMetricUpdate> getWfMetricWindows(String wfSpecName, int
// wfSpecVersion, Date time) {
// List<WfMetricUpdate> out = new ArrayList<>();
// out.add(getWmUpdate(time, MetricsWindowLength.MINUTES_5, wfSpecName,
// wfSpecVersion));
// out.add(getWmUpdate(time, MetricsWindowLength.HOURS_2, wfSpecName,
// wfSpecVersion));
// out.add(getWmUpdate(time, MetricsWindowLength.DAYS_1, wfSpecName,
// wfSpecVersion));
// return out;
// }

// private WfMetricUpdate getWmUpdate(
// Date windowStart, MetricsWindowLength type, String wfSpecName, int
// wfSpecVersion) {
// windowStart = LHUtil.getWindowStart(windowStart, type);
// String id = WfMetricUpdate.getObjectId(type, windowStart, wfSpecName,
// wfSpecVersion);
// if (wfMetricPuts.containsKey(id)) {
// return wfMetricPuts.get(id);
// }

// WfMetricUpdate out = rocksdb.get(id, WfMetricUpdate.class);
// if (out == null) {
// out = new WfMetricUpdate();
// out.windowStart = windowStart;
// out.type = type;
// out.wfSpecName = wfSpecName;
// out.wfSpecVersion = wfSpecVersion;
// }

// wfMetricPuts.put(id, out);
// return out;
// }

// public List<TaskMetricUpdate> getTaskMetricWindows(String taskDefName, Date
// time) {
// List<TaskMetricUpdate> out = new ArrayList<>();
// out.add(getTmUpdate(time, MetricsWindowLength.MINUTES_5, taskDefName));
// out.add(getTmUpdate(time, MetricsWindowLength.HOURS_2, taskDefName));
// out.add(getTmUpdate(time, MetricsWindowLength.DAYS_1, taskDefName));
// return out;
// }

// private TaskMetricUpdate getTmUpdate(Date windowStart, MetricsWindowLength
// type, String taskDefName) {
// windowStart = LHUtil.getWindowStart(windowStart, type);
// String id = TaskMetricUpdate.getStoreKey(type, windowStart, taskDefName);
// if (taskMetricPuts.containsKey(id)) {
// return taskMetricPuts.get(id);
// }

// TaskMetricUpdate out = rocksdb.get(id, TaskMetricUpdate.class);
// if (out == null) {
// out = new TaskMetricUpdate();
// out.windowStart = windowStart;
// out.type = type;
// out.taskDefName = taskDefName;
// }

// taskMetricPuts.put(id, out);
// return out;
// }

// public void forwardAndClearMetricsUpdatesUntil() {
// Map<String, TaskMetricUpdate> clusterTaskUpdates = new HashMap<>();

// try (LHKeyValueIterator<TaskMetricUpdate> iter = rocksdb.range("", "~",
// TaskMetricUpdate.class);) {
// while (iter.hasNext()) {
// LHIterKeyValue<TaskMetricUpdate> next = iter.next();

// log.debug("Sending out metrics for {}", next.getKey());

// rocksdb.delete(next.getKey());
// TaskMetricUpdate tmu = next.getValue();
// forwardTaskMetricUpdate(tmu);

// // Update the cluster-level metrics
// String clusterTaskUpdateKey = TaskMetricUpdate.getStoreKey(tmu.type,
// tmu.windowStart,
// LHConstants.CLUSTER_LEVEL_METRIC);
// TaskMetricUpdate clusterTaskUpdate;
// if (clusterTaskUpdates.containsKey(clusterTaskUpdateKey)) {
// clusterTaskUpdate = clusterTaskUpdates.get(clusterTaskUpdateKey);
// } else {
// clusterTaskUpdate = new TaskMetricUpdate(tmu.windowStart, tmu.type,
// LHConstants.CLUSTER_LEVEL_METRIC);
// }

// clusterTaskUpdate.merge(tmu);
// clusterTaskUpdates.put(clusterTaskUpdateKey, clusterTaskUpdate);
// rocksdb.delete(tmu);
// }
// }

// // Forward the cluster level task updates
// for (TaskMetricUpdate tmu : clusterTaskUpdates.values()) {
// forwardTaskMetricUpdate(tmu);
// }

// // get ready to update the cluster level WF Metrics
// Map<String, WfMetricUpdate> clusterWfUpdates = new HashMap<>();

// try (LHKeyValueIterator<WfMetricUpdate> iter = rocksdb.range("", "~",
// WfMetricUpdate.class);) {
// while (iter.hasNext()) {
// LHIterKeyValue<WfMetricUpdate> next = iter.next();
// WfMetricUpdate wmu = next.getValue();
// forwardWfMetricUpdate(wmu);

// // Update the cluster-level metrics
// String clusterWfUpdateKey = WfMetricUpdate.getStoreKey(wmu.type,
// wmu.windowStart,
// LHConstants.CLUSTER_LEVEL_METRIC, 0);
// WfMetricUpdate clusterWfUpdate;
// if (clusterWfUpdates.containsKey(clusterWfUpdateKey)) {
// clusterWfUpdate = clusterWfUpdates.get(clusterWfUpdateKey);
// } else {
// clusterWfUpdate = new WfMetricUpdate(wmu.windowStart, wmu.type,
// LHConstants.CLUSTER_LEVEL_METRIC,
// 0);
// }
// clusterWfUpdate.merge(wmu);
// clusterWfUpdates.put(clusterWfUpdateKey, clusterWfUpdate);

// rocksdb.delete(wmu);
// }
// }

// // Forward the cluster level task updates
// for (WfMetricUpdate wmu : clusterWfUpdates.values()) {
// forwardWfMetricUpdate(wmu);
// }
// }

// private void forwardTaskMetricUpdate(TaskMetricUpdate tmu) {
// CommandProcessorOutput cpo = new CommandProcessorOutput();
// cpo.partitionKey = tmu.getPartitionKey();
// cpo.topic = config.getRepartitionTopicName();
// cpo.payload = new RepartitionCommand(tmu, new Date(), tmu.getPartitionKey());
// Record<String, CommandProcessorOutput> out = new
// Record<>(tmu.getPartitionKey(), cpo,
// System.currentTimeMillis());
// ctx.forward(out);
// }

// private void forwardWfMetricUpdate(WfMetricUpdate wmu) {
// CommandProcessorOutput cpo = new CommandProcessorOutput();
// cpo.partitionKey = wmu.getPartitionKey();
// cpo.topic = config.getRepartitionTopicName();
// cpo.payload = new RepartitionCommand(wmu, new Date(), wmu.getPartitionKey());
// Record<String, CommandProcessorOutput> out = new
// Record<>(wmu.getPartitionKey(), cpo,
// System.currentTimeMillis());
// ctx.forward(out);
// }
