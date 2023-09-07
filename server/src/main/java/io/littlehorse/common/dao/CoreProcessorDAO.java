package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.streams.store.ReadOnlyRocksDBWrapper;
import io.littlehorse.server.streams.util.InternalHosts;
import io.littlehorse.server.streams.util.MetadataCache;
import java.util.Date;

public abstract class CoreProcessorDAO extends ReadOnlyMetadataStore {

    public CoreProcessorDAO(ReadOnlyRocksDBWrapper rocksdb, MetadataCache metadataCache) {
        super(rocksdb, metadataCache);
    }

    /*
     * Lifecycle for processing a Command
     */

    public abstract void initCommand(CommandModel command);

    public abstract CommandModel getCommand();

    public abstract void commit();

    /*
     * Basic CRUD for CoreGetables
     */

    public abstract <U extends Message, T extends CoreGetable<U>> T get(ObjectIdModel<?, U, T> id);

    public abstract void put(CoreGetable<?> getable);

    public abstract void delete(WfRunIdModel id);

    public abstract void delete(ExternalEventIdModel id);

    /*
     * One-off operations related to WfRun Processing
     */

    public abstract ExternalEventModel getUnclaimedEvent(String wfRunId, String externalEventDefName);

    public abstract void scheduleTask(ScheduledTaskModel scheduledTask);

    public abstract void scheduleTimer(LHTimer timer);

    public abstract ScheduledTaskModel markTaskAsScheduled(TaskRunIdModel taskRunId);

    public WfRunModel getWfRun(String id) {
        return get(new WfRunIdModel(id));
    }

    /*
     * Misc. This will be organized further in the future.
     */

    public abstract String getCoreCmdTopic();

    public Date getEventTime() {
        return getCommand().time;
    }

    public abstract AnalyticsRegistry getRegistry();

    public abstract LHHostInfo getAdvertisedHost(HostModel host, String listenerName);

    public abstract InternalHosts getInternalHosts();
}
