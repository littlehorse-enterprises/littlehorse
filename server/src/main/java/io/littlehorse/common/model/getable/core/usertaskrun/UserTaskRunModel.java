package io.littlehorse.common.model.getable.core.usertaskrun;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreOutputTopicGetable;
import io.littlehorse.common.model.corecommand.subcommand.CompleteUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.DeadlineReassignUserTaskModel;
import io.littlehorse.common.model.corecommand.subcommand.SaveUserTaskRunProgressRequestModel;
import io.littlehorse.common.model.getable.core.noderun.NodeFailureException;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UTEAssignedModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UTECancelledModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UTESavedModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UserTaskEventModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.common.model.getable.core.wfrun.failure.FailureModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.UserTaskNodeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UTActionTriggerModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskFieldModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Getter
@Setter
public class UserTaskRunModel extends CoreOutputTopicGetable<UserTaskRun> {

    private UserTaskRunIdModel id;
    private UserTaskDefIdModel userTaskDefId;

    private List<UserTaskEventModel> events = new ArrayList<>();

    private Map<String, VariableValueModel> results = new HashMap<>();
    private String userGroup;
    private String userId;

    private UserTaskRunStatus status;
    private String notes;
    private Date scheduledTime;
    private NodeRunIdModel nodeRunId;
    private int epoch;

    // Below are non-proto fields
    private UserTaskNodeModel userTaskNode;
    private ExecutionContext executionContext;
    private ProcessorExecutionContext processorContext;
    private FailureModel failureToThrowKenobi;

    public UserTaskRunModel() {}

    public UserTaskRunModel(ProcessorExecutionContext processorContext) {
        this.processorContext = processorContext;
    }

    public UserTaskRunModel(
            UserTaskDefModel utd,
            UserTaskNodeModel userTaskNode,
            NodeRunModel nodeRunModel,
            ProcessorExecutionContext processorContext) {
        this.userTaskDefId = utd.getObjectId();
        this.nodeRunId = nodeRunModel.getObjectId();
        this.id = new UserTaskRunIdModel(nodeRunId.getWfRunId());
        this.scheduledTime = new Date();
        this.epoch = 0;
        this.userTaskNode = userTaskNode;
        this.executionContext = processorContext;
        this.processorContext = processorContext;
    }

    @Override
    public Class<UserTaskRun> getProtoBaseClass() {
        return UserTaskRun.class;
    }

    @Override
    public UserTaskRun.Builder toProto() {
        UserTaskRun.Builder out = UserTaskRun.newBuilder()
                .setStatus(status)
                .setId(id.toProto())
                .setUserTaskDefId(userTaskDefId.toProto())
                .setScheduledTime(LHUtil.fromDate(scheduledTime))
                .setNodeRunId(nodeRunId.toProto());

        if (userId != null) out.setUserId(userId);
        if (userGroup != null) out.setUserGroup(userGroup);

        if (notes != null) out.setNotes(notes);

        for (UserTaskEventModel event : events) {
            out.addEvents(event.toProto());
        }
        for (Map.Entry<String, VariableValueModel> result : results.entrySet()) {
            out.putResults(result.getKey(), result.getValue().toProto().build());
        }
        out.setEpoch(this.epoch);

        return out;
    }

    @Override
    public UserTaskRunIdModel getObjectId() {
        return id;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UserTaskRun p = (UserTaskRun) proto;
        id = LHSerializable.fromProto(p.getId(), UserTaskRunIdModel.class, context);
        userTaskDefId = LHSerializable.fromProto(p.getUserTaskDefId(), UserTaskDefIdModel.class, context);
        status = p.getStatus();
        scheduledTime = LHLibUtil.fromProtoTs(p.getScheduledTime());
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class, context);

        if (p.hasUserId()) userId = p.getUserId();
        if (p.hasUserGroup()) userGroup = p.getUserGroup();

        if (p.hasNotes()) notes = p.getNotes();

        for (UserTaskEvent ute : p.getEventsList()) {
            events.add(LHSerializable.fromProto(ute, UserTaskEventModel.class, context));
        }
        for (Map.Entry<String, VariableValue> result : p.getResultsMap().entrySet()) {
            results.put(result.getKey(), VariableValueModel.fromProto(result.getValue(), context));
        }
        this.epoch = p.getEpoch();
        this.executionContext = context;
        this.processorContext = context.castOnSupport(ProcessorExecutionContext.class);
    }

    public boolean isTerminated() {
        return status == UserTaskRunStatus.DONE || status == UserTaskRunStatus.CANCELLED;
    }

    public Date getCreatedAt() {
        return scheduledTime;
    }

    public boolean advanceIfPossible(Date time) {
        // User Tasks currently only care about the input from the workers, not the
        // other threads.
        return false;
    }

    private UserTaskNodeModel getUtNode() {
        return getNodeRun().getNode().getUserTaskNode();
    }

    public void assignTo(String newUserId, String newUserGroup, boolean canScheduleActions) {
        String oldUserId = this.userId;
        String oldUserGroup = this.userGroup;

        this.userId = newUserId;
        this.userGroup = newUserGroup;
        this.epoch += 1;

        // If the assignment changed, then we need to schedule any triggers.
        if (canScheduleActions && !Objects.equals(newUserId, oldUserId) && newUserId != null) {
            for (UTActionTriggerModel trigger : getUtNode().getActions(UTHook.ON_TASK_ASSIGNED)) {
                try {
                    scheduleAction(trigger);
                } catch (LHVarSubError exn) {
                    // For now, we shouldn't fail the workflow. We can handle it better in the future.
                    log.error("Failed scheduling user task action: {}", exn);
                }
            }
        }

        // Log the assigment.
        UTEAssignedModel assignedEvent = new UTEAssignedModel(oldUserId, newUserId, oldUserGroup, newUserGroup);
        events.add(new UserTaskEventModel(
                assignedEvent, processorContext.currentCommand().getTime()));

        if (this.userId != null) {
            this.status = UserTaskRunStatus.ASSIGNED;
        } else {
            this.status = UserTaskRunStatus.UNASSIGNED;
        }
    }

    public void onArrival(Date time) throws NodeFailureException {
        UserTaskNodeModel node = getNodeRun().getNode().getUserTaskNode();
        status = UserTaskRunStatus.UNASSIGNED;

        // Need to either assign to a user or to a group.
        try {
            if (node.getNotes() != null) {
                VariableValueModel notesVal = getNodeRun()
                        .getThreadRun()
                        .assignVariable(node.getNotes())
                        .asStr();

                notes = notesVal.getStrVal();
            }
            ThreadRunModel thread = getNodeRun().getThreadRun();

            String newUserId = node.getUserId() == null
                    ? null
                    : thread.assignVariable(node.getUserId()).asStr().getStrVal();
            String newUserGroup = node.getUserGroup() == null
                    ? null
                    : thread.assignVariable(node.getUserGroup()).asStr().getStrVal();

            if (newUserId == null && newUserGroup == null) {
                throw new NodeFailureException(new FailureModel("Invalid user task assignment", LHConstants.VAR_ERROR));
            }

            if (newUserId != null && newUserId.trim().isEmpty()) {
                throw new NodeFailureException(
                        new FailureModel("Invalid user task assignment. UserId can't be empty", LHConstants.VAR_ERROR));
            }

            if (newUserGroup != null && newUserGroup.trim().isEmpty()) {
                throw new NodeFailureException(new FailureModel(
                        "Invalid group task assignment. UserGroup can't be empty", LHConstants.VAR_ERROR));
            }

            // Set owners and schedule all on-assignment hooks
            this.assignTo(newUserId, newUserGroup, true);

            // Schedule all on-arrival hooks.
            for (UTActionTriggerModel action : getUtNode().getActions(UTHook.ON_ARRIVAL)) {
                scheduleAction(action);
            }
            log.trace("Arrived at user task: {}", this);
        } catch (LHVarSubError exn) {
            FailureModel failure = new FailureModel(
                    "Invalid variables when creating UserTaskRun: " + exn.getMessage(), LHConstants.VAR_SUB_ERROR);
            throw new NodeFailureException(failure);
        }
    }

    private void scheduleAction(UTActionTriggerModel trigger) throws LHVarSubError {
        trigger.schedule(this, processorContext);
    }

    public void deadlineReassign(DeadlineReassignUserTaskModel trigger) {
        if (status != UserTaskRunStatus.ASSIGNED || this.epoch != trigger.getEpoch()) {
            log.debug("Not doing deadline reassignment on UT. Status {}", status);
            return;
        }

        String newUserGroup = null;
        String newUserId = null;

        ThreadRunModel thread = getNodeRun().getThreadRun();
        try {
            if (trigger.getNewUserGroup() != null) {
                newUserGroup =
                        thread.assignVariable(trigger.getNewUserGroup()).asStr().getStrVal();
            }
            if (trigger.getNewUserId() != null) {
                newUserId =
                        thread.assignVariable(trigger.getNewUserId()).asStr().getStrVal();
            }
        } catch (LHVarSubError exn) {
            this.failureToThrowKenobi = new FailureModel(
                    "Failed calculating new assignment for UserTaskRun: " + exn.getMessage(),
                    LHErrorType.VAR_SUB_ERROR.toString());
            return;
        }

        if (newUserId == null && newUserGroup == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Both userId and userGroup are null on reassignment.");
        }

        this.assignTo(newUserId, newUserGroup, true);
    }

    public void cancel() {
        this.status = UserTaskRunStatus.CANCELLED;
        this.events.add(new UserTaskEventModel(
                new UTECancelledModel("UserTaskRun was cancelled"),
                processorContext.currentCommand().getTime()));
        ThreadRunModel currentThreadRun = this.getNodeRun().getThreadRun();
        String failureName = this.getUtNode().assignExceptionNameVariable(currentThreadRun);
        failureToThrowKenobi = new FailureModel("User task cancelled", failureName);
    }

    public void processProgressSavedEvent(SaveUserTaskRunProgressRequestModel req, ProcessorExecutionContext ctx)
            throws LHApiException {
        if (isTerminated()) {
            throw new LHApiException(Status.FAILED_PRECONDITION, "UserTaskRun is in status " + status);
        }

        this.results = req.getResults();
        UTESavedModel saved = new UTESavedModel(req.getUserId(), req.getResults());
        this.events.add(new UserTaskEventModel(saved, ctx.currentCommand().getTime()));
    }

    public void processTaskCompletedEvent(CompleteUserTaskRunRequestModel event) throws LHApiException {
        if (isTerminated()) {
            log.warn("Tried to complete a user task that was not running");
            return;
        }

        if (event.getUserId() != null && !event.getUserId().equals(userId)) {
            log.trace("Complete User Task Run event had different ID, adding reassignment");

            // Note: currently, the CompleteUserTaskRun doesn't take in a group. So we don't
            // change the group.

            // The task is being completed, so we don't want to schedule any hooks.
            boolean scheduleHooks = false;
            this.assignTo(event.getUserId(), this.userGroup, scheduleHooks);
        }

        UserTaskDefModel userTaskDef = executionContext
                .metadataManager()
                .get(new UserTaskDefIdModel(
                        getUserTaskDefId().getName(), getUserTaskDefId().getVersion()));

        Map<String, UserTaskFieldModel> userTaskFieldsGroupedByName = userTaskDef.getFields().stream()
                .collect(Collectors.toMap(UserTaskFieldModel::getName, Function.identity()));

        for (Map.Entry<String, VariableValueModel> field : event.getResults().entrySet()) {
            UserTaskFieldModel userTaskFieldFromTaskDef = userTaskFieldsGroupedByName.get(field.getKey());
            boolean isUndefined = userTaskFieldFromTaskDef == null
                    || !userTaskFieldFromTaskDef
                            .getType()
                            .equals(field.getValue().getType());
            if (isUndefined) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Field [name = %s, type = %s] is not defined in UserTask schema or has different type"
                                .formatted(field.getKey(), field.getValue().getType()));
            }
            results.put(field.getKey(), field.getValue());
        }
        validateMandatoryFieldsFromCompletedEvent(
                userTaskFieldsGroupedByName.values(), event.getResults().keySet());
        this.status = UserTaskRunStatus.DONE;
    }

    private void validateMandatoryFieldsFromCompletedEvent(
            Collection<UserTaskFieldModel> userTaskFieldsFromTaskDef, Collection<String> inputFieldNames)
            throws LHApiException {
        List<String> mandatoryFieldNames = userTaskFieldsFromTaskDef.stream()
                .filter(UserTaskFieldModel::isRequired)
                .map(UserTaskFieldModel::getName)
                .toList();
        String mandatoryFieldsNotFound = mandatoryFieldNames.stream()
                .filter(Predicate.not(inputFieldNames::contains))
                .collect(Collectors.joining(","));
        if (!mandatoryFieldsNotFound.isEmpty()) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "[%s] are mandatory fields".formatted(mandatoryFieldsNotFound));
        }
    }

    public NodeRunModel getNodeRun() {
        return processorContext.getableManager().get(nodeRunId);
    }

    // TODO: LH-314
    public void processTaskSavedEvent() {
        throw new NotImplementedException();
    }

    private List<List<String>> getIndexHelper() {
        // We need all 15 combinations of the 4 tags, except for the combination
        // that has none of them set. ((2^4) - 1) == 15, so there are 15 entries.
        return List.of(
                List.of("status"),
                List.of("status", "userTaskDefName"),
                List.of("status", "userTaskDefName", "userId"),
                List.of("status", "userTaskDefName", "userId", "userGroup"),
                List.of("status", "userTaskDefName", "userGroup"),
                List.of("status", "userGroup"),
                List.of("status", "userId", "userGroup"),
                List.of("status", "userId"),
                List.of("userTaskDefName"),
                List.of("userTaskDefName", "userId"),
                List.of("userTaskDefName", "userId", "userGroup"),
                List.of("userTaskDefName", "userGroup"),
                List.of("userId"),
                List.of("userId", "userGroup"),
                List.of("userGroup"));
    }

    @Override
    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        List<GetableIndex<? extends AbstractGetable<?>>> out = new ArrayList<>(15);
        for (List<String> index : getIndexHelper()) {
            Predicate<UserTaskRunModel> isIndexActive = utr -> {
                if (index.contains("userId") && utr.getUserId() == null) return false;
                if (index.contains("userGroup") && utr.getUserGroup() == null) return false;
                return true;
            };

            List<Pair<String, GetableIndex.ValueType>> attributes = index.stream()
                    .map(attrib -> Pair.of(attrib, GetableIndex.ValueType.SINGLE))
                    .toList();
            out.add(new GetableIndex<UserTaskRunModel>(attributes, Optional.of(TagStorageType.LOCAL), isIndexActive));
        }

        return out;
    }

    public static TagStorageType tagStorageTypeForStatus(UserTaskRunStatus status) {
        return TagStorageType.LOCAL;
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "status" -> {
                return List.of(new IndexedField(key, this.getStatus().toString(), TagStorageType.LOCAL));
            }
            case "userTaskDefName" -> {
                return List.of(new IndexedField(
                        key, this.getUserTaskDefId().getName(), tagStorageType.get() // Is this right?
                        ));
            }
            case "userId" -> {
                return List.of(new IndexedField(key, this.getUserId(), TagStorageType.LOCAL));
            }
            case "userGroup" -> {
                return List.of(new IndexedField(key, this.getUserGroup(), tagStorageType.get()));
            }
        }
        log.warn("Tried to get value for unknown index field {}", key);
        return List.of();
    }
}
