package io.littlehorse.common.dao;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.command.CommandModel;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.sdk.common.proto.HostInfo;
import io.littlehorse.server.streams.util.InternalHosts;
import java.util.Date;

public interface CoreProcessorDAO extends ReadOnlyMetadataStore {

    /*
     * Lifecycle for processing a Command
     */

    public void initCommand(CommandModel command);

    public CommandModel getCommand();

    public void commit();

    /*
     * Basic CRUD for CoreGetables
     */

    public <U extends Message, T extends CoreGetable<U>> T get(ObjectIdModel<?, U, T> id);

    public void put(CoreGetable<?> getable);

    public DeleteObjectReply delete(WfRunIdModel id);

    public DeleteObjectReply delete(ExternalEventIdModel id);

    /*
     * One-off operations related to WfRun Processing
     */

    public ExternalEventModel getUnclaimedEvent(String wfRunId, String externalEventDefName);

    public void scheduleTask(ScheduledTaskModel scheduledTask);

    public void scheduleTimer(LHTimer timer);

    public ScheduledTaskModel markTaskAsScheduled(TaskRunIdModel taskRunId);

    default WfRunModel getWfRun(String id) {
        return get(new WfRunIdModel(id));
    }

    /*
     * Misc. This will be organized further in the future.
     */

    public String getCoreCmdTopic();

    public default Date getEventTime() {
        return getCommand().time;
    }

    public AnalyticsRegistry getRegistry();

    public HostInfo getAdvertisedHost(HostModel host, String listenerName) throws LHBadRequestError, LHConnectionError;

    public InternalHosts getInternalHosts();
}
