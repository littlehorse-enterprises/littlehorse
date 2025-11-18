package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Empty;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.ExternalEventNodeRunModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.objectId.NodeOutputIdModel;
import io.littlehorse.common.model.getable.objectId.VariableIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.InternalDeleteWfRunRequest;
import io.littlehorse.common.proto.InternalDeleteWfRunRequest.DeleteWfRunBookmark;
import io.littlehorse.sdk.common.proto.DeleteWfRunRequest;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@Slf4j
public class InternalDeleteWfRunRequestModel extends CoreSubCommand<InternalDeleteWfRunRequest> {

    private WfRunIdModel wfRunId;
    private DeleteWfRunBookmark bookmark;

    public InternalDeleteWfRunRequestModel() {}

    public InternalDeleteWfRunRequestModel(DeleteWfRunRequest publicRequest) {
        this.wfRunId = LHSerializable.fromProto(publicRequest.getId(), WfRunIdModel.class, null);
    }

    public Class<InternalDeleteWfRunRequest> getProtoBaseClass() {
        return InternalDeleteWfRunRequest.class;
    }

    public InternalDeleteWfRunRequest.Builder toProto() {
        InternalDeleteWfRunRequest.Builder out =
                InternalDeleteWfRunRequest.newBuilder().setId(wfRunId.toProto());
        if (bookmark != null) out.setBookmark(bookmark);
        return out;
    }

    public void initFrom(Message proto, ExecutionContext context) {
        InternalDeleteWfRunRequest p = (InternalDeleteWfRunRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getId(), WfRunIdModel.class, context);
        if (p.hasBookmark()) this.bookmark = p.getBookmark();
    }

    public String getPartitionKey() {
        return wfRunId.getPartitionKey().get();
    }

    @Override
    public Empty process(CoreProcessorContext ctx, LHServerConfig config) {
        GetableManager manager = ctx.getableManager();
        WfRunModel wfRun = manager.get(wfRunId);
        if (wfRun == null) return Empty.getDefaultInstance();

        DeleteWfRunBookmark resultToResumeFrom =
                cleanupAsMuchAsPossible(wfRun, manager, ctx, config.getMaxDeletesPerCommand());
        if (resultToResumeFrom != null) {
            InternalDeleteWfRunRequestModel resumeCommand = new InternalDeleteWfRunRequestModel();
            resumeCommand.setWfRunId(wfRunId);
            resumeCommand.setBookmark(resultToResumeFrom);
            LHTimer boomerangCommand = new LHTimer(new CommandModel(resumeCommand));
            boomerangCommand.setMaturationTime(new Date()); // No need to delay: just boomerang immediately
            ctx.getTaskManager().scheduleTimer(boomerangCommand);
            log.debug("Scheduled timer to resume cleaning up WfRun");
        }
        return Empty.getDefaultInstance();
    }

    private DeleteWfRunBookmark cleanupAsMuchAsPossible(
            WfRunModel wfRun, GetableManager manager, CoreProcessorContext ctx, int maxDeletesInOneCommand) {
        int thingsDone = 0;
        int startingThread;
        int startingNodeRun;
        if (bookmark == null) {
            startingThread = 0;
            startingNodeRun = 0;
        } else {
            startingThread = bookmark.getLastThreadRunNumber();
            startingNodeRun = bookmark.getLastNodeRunPosition();
        }

        for (int threadRunNumber = startingThread;
                threadRunNumber < wfRun.getThreadRunsUseMeCarefully().size();
                threadRunNumber++) {
            ThreadRunModel thread = wfRun.getThreadRunsUseMeCarefully().get(threadRunNumber);
            for (int nodeRunPosition = startingNodeRun;
                    nodeRunPosition <= thread.getCurrentNodePosition();
                    nodeRunPosition++) {
                thingsDone++;
                NodeRunModel nodeRun = thread.getNodeRun(nodeRunPosition);

                if (nodeRun == null) continue;

                // Delete things created by the NodeRun, eg TaskRun / UserTaskRun / WorkflowEvent
                List<? extends CoreObjectId<?, ?, ?>> createdGetables =
                        nodeRun.getSubNodeRun().getCreatedSubGetableIds(ctx);

                for (CoreObjectId<?, ?, ?> createdGetable : createdGetables) {
                    manager.delete((CoreObjectId<?, ?, ?>) createdGetable);
                }

                // ExternalEventNodeRun's can create correlation markers. Deleting those are tricky.
                if (nodeRun.getExternalEventRun() != null) {
                    ExternalEventNodeRunModel extEvtNr = nodeRun.getExternalEventRun();
                    if (extEvtNr.getCorrelationKey() != null) {
                        // If we delete the WfRun, we should remove the WfRun from the correlation marker.
                        extEvtNr.sendRemoveCorrelationMarkerCommand(ctx);
                    }
                }

                // Delete the NodeRun
                manager.delete(nodeRun.getObjectId());

                // Delete the NodeOutput if it exists
                NodeOutputIdModel nodeOutputId =
                        new NodeOutputIdModel(wfRunId, thread.getNumber(), nodeRun.getNodeName());
                manager.delete(nodeOutputId);

                if (thingsDone >= maxDeletesInOneCommand) {
                    log.debug("Not done deleting nodeRuns for {}", wfRunId);
                    DeleteWfRunBookmark.Builder result = DeleteWfRunBookmark.newBuilder()
                            .setLastThreadRunNumber(threadRunNumber)
                            .setLastNodeRunPosition(nodeRunPosition + 1);
                    return result.build();
                }
            }

            startingNodeRun = 0;

            // Delete the variables belonging to that ThreadRun
            ThreadSpecModel threadSpec = thread.getThreadSpec();
            for (ThreadVarDefModel varDef : threadSpec.getVariableDefs()) {
                if (varDef.getAccessLevel() == WfRunVariableAccessLevel.INHERITED_VAR) continue;

                VariableIdModel id = new VariableIdModel(
                        wfRunId, thread.getNumber(), varDef.getVarDef().getName());
                manager.delete(id);
            }
        }

        boolean deletedAllEvents = manager.tryToDeleteAllExternalEventsFor(wfRunId, maxDeletesInOneCommand);
        if (!deletedAllEvents) {
            log.debug("Not done deleting external events for {}", wfRunId);
            return DeleteWfRunBookmark.newBuilder()
                    .setLastThreadRunNumber(Integer.MAX_VALUE)
                    .build();
        }

        log.trace("Completing WfRun deletion for {}", wfRunId);
        manager.delete(wfRunId);
        return null;
    }

    public static InternalDeleteWfRunRequestModel fromProto(InternalDeleteWfRunRequest p, ExecutionContext context) {
        InternalDeleteWfRunRequestModel out = new InternalDeleteWfRunRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
