package io.littlehorse.common.model.getable.core.usertaskrun;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.CoreGetable;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.corecommand.subcommand.CompleteUserTaskRunRequestModel;
import io.littlehorse.common.model.corecommand.subcommand.ReassignUserTask;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UTEAssignedModel;
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
import io.littlehorse.common.proto.ReassignedUserTaskPb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.IndexedField;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
public class UserTaskRunModel extends CoreGetable<UserTaskRun> {

    private UserTaskRunIdModel id;
    private UserTaskDefIdModel userTaskDefId;

    private List<UserTaskEventModel> events = new ArrayList<>();

    private Map<String, VariableValueModel> results = new HashMap<>();
    private String userGroup;
    private String userId;

    private UserTaskRunStatus status;
    private String notes;
    private Date scheduledTime;

    // If we ever allow ad-hoc User Tasks, this will move to an optional
    // field, or a `oneof user_task_source` field. However, note that such
    // a change would be fine from the API Compatibility perspective.
    private NodeRunIdModel nodeRunId;

    // Below are non-proto fields
    private UserTaskNodeModel userTaskNode;

    public UserTaskRunModel() {}

    public UserTaskRunModel(UserTaskDefModel utd, UserTaskNodeModel userTaskNode, NodeRunModel nodeRunModel) {
        this.userTaskDefId = utd.getObjectId();
        this.nodeRunId = nodeRunModel.getObjectId();
        this.id = new UserTaskRunIdModel(nodeRunId.getWfRunId());
        this.scheduledTime = new Date();
        this.userTaskNode = userTaskNode;
    }

    public Class<UserTaskRun> getProtoBaseClass() {
        return UserTaskRun.class;
    }

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

        return out;
    }

    public UserTaskRunIdModel getObjectId() {
        return id;
    }

    public void initFrom(Message proto) {
        UserTaskRun p = (UserTaskRun) proto;
        id = LHSerializable.fromProto(p.getId(), UserTaskRunIdModel.class);
        userTaskDefId = LHSerializable.fromProto(p.getUserTaskDefId(), UserTaskDefIdModel.class);
        status = p.getStatus();
        scheduledTime = LHLibUtil.fromProtoTs(p.getScheduledTime());
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunIdModel.class);

        if (p.hasUserId()) userId = p.getUserId();
        if (p.hasUserGroup()) userGroup = p.getUserGroup();

        if (p.hasNotes()) notes = p.getNotes();

        for (UserTaskEvent ute : p.getEventsList()) {
            events.add(LHSerializable.fromProto(ute, UserTaskEventModel.class));
        }
        for (Map.Entry<String, VariableValue> result : p.getResultsMap().entrySet()) {
            results.put(result.getKey(), VariableValueModel.fromProto(result.getValue()));
        }
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

    private void assignTo(String newUserId, String newUserGroup, boolean canScheduleActions) throws LHVarSubError {
        String oldUserId = this.userId;
        String oldUserGroup = this.userGroup;

        // If the assignment changed, then we need to schedule any triggers.
        if (canScheduleActions && !Objects.equals(newUserId, oldUserId) && newUserId != null) {
            for (UTActionTriggerModel trigger : getUtNode().getActions(UTHook.ON_TASK_ASSIGNED)) {
                scheduleAction(trigger);
            }
        }

        // Log the assigment.
        UTEAssignedModel assignedEvent = new UTEAssignedModel(oldUserId, newUserId, oldUserGroup, newUserGroup);
        events.add(new UserTaskEventModel(assignedEvent, getDao().getEventTime()));

        if (this.userId != null) {
            this.status = UserTaskRunStatus.ASSIGNED;
        } else {
            this.status = UserTaskRunStatus.UNASSIGNED;
        }
    }

    public void onArrival(Date time) {
        UserTaskNodeModel node = getNodeRun().getNode().getUserTaskNode();
        getNodeRun().status = LHStatus.RUNNING;
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
            getNodeRun().fail(failure, time);
        }
    }

    private void scheduleAction(UTActionTriggerModel trigger) throws LHVarSubError {
        trigger.schedule(getNodeRun().getThreadRun().wfRun.getDao(), this);
    }

    public void deadlineReassign(String newOwner, ReassignedUserTaskPb.AssignToCase assignToCase) {
        UTEAssignedModel reassigned = null;
        UserModel user = new UserModel(newOwner, this.getUserGroup());
        switch (assignToCase) {
            case USER_ID:
                reassigned = reassignToUser(user, false);
                break;
            case USER_GROUP:
                reassigned = reassignToUserGroup(new UserGroupModel(newOwner));
            case ASSIGNTO_NOT_SET:
        }
        if (reassigned != null) {
            events.add(new UserTaskEventModel(reassigned, new Date()));
        }
    }

    public void cancel() {
        status = UserTaskRunStatus.CANCELLED;
        FailureModel failure = new FailureModel("User task cancelled", LHConstants.USER_TASK_CANCELLED);
        getNodeRun().fail(failure, new Date());
    }

    private void scheduleTaskReassign(UTActionTriggerModel action) {
        long delayInSeconds = action.getDelaySeconds().getRhsLiteralValue().intVal;
        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(delayInSeconds);
        Date maturationTime =
                Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        ReassignUserTask command = buildReassignUserTaskCommandFrom(action);
        if (command != null) {
            LHTimer timer = new LHTimer(new CommandModel(command, maturationTime), getDao());
            getDao().scheduleTimer(timer);
        }
    }

    private ReassignUserTask buildReassignUserTaskCommandFrom(UTActionTriggerModel action) {
        ReassignedUserTaskPb.AssignToCase assignToCase = null;
        switch (action.getReassign().getAssignToCase()) {
            case USER_ID:
                assignToCase = ReassignedUserTaskPb.AssignToCase.USER_ID;
                break;
            case USER_GROUP:
                assignToCase = ReassignedUserTaskPb.AssignToCase.USER_GROUP;
                break;
            case ASSIGNTO_NOT_SET:
                log.warn("Invalid reassignment: no reassign_to set!");
                break;
        }
        FailureModel invalidAssignToCaseFailure = new FailureModel(
                "Invalid variables when creating UserTaskRun: Invalid Assign to case", LHConstants.VAR_SUB_ERROR);

        if (assignToCase == null) {
            getNodeRun().fail(invalidAssignToCaseFailure, new Date());
            return null;
        }
        try {
            VariableValueModel variableValueModel = getNodeRun()
                    .getThreadRun()
                    .assignVariable(action.getReassign().getNewOwner())
                    .asStr();
            return new ReassignUserTask(getNodeRun().getObjectId(), variableValueModel.getStrVal(), assignToCase);
        } catch (LHVarSubError ex) {
            FailureModel invalidVariablesFailure = new FailureModel(
                    "Invalid variables when creating UserTaskRun: " + ex.toString(), LHConstants.VAR_SUB_ERROR);
            getNodeRun().fail(invalidVariablesFailure, new Date());
            return null;
        }
    }

    public void processTaskCompletedEvent(CompleteUserTaskRunRequestModel event) throws LHApiException {
        if (getNodeRun().getStatus() != LHStatus.STARTING && getNodeRun().getStatus() != LHStatus.RUNNING) {
            log.warn("Tried to complete a user task that was not running");
            return;
        }

        if (event.getUserId() != null && !event.getUserId().equals(userId))  {
            log.trace("Complete User Task Run event had different ID, adding reassignment");

            // Note: currently, the CompleteUserTaskRun doesn't take in a group. So we don't
            // change the group.

            // The task is being completed, so we don't want to schedule any hooks.
            boolean scheduleHooks = false;
            try {
                this.assignTo(event.getUserId(), this.userGroup, scheduleHooks);
            } catch (LHVarSubError impossible) {
                // This error is pretty impossible to get, since the LHVarSubError comes from
                // the scheduleAction call, and we are not scheduling the hooks.
                throw new LHApiException(Status.INTERNAL, impossible);
            }
        }

        Map<String, Object> rawNodeOutput = new HashMap<>();
        UserTaskDefModel userTaskDef = getDao().getUserTaskDef(
                        getUserTaskDefId().getName(), getUserTaskDefId().getVersion());

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
                                .formatted(
                                        field.getKey(),
                                        field.getValue().getType()));
            }
            results.put(field.getKey(), field.getValue());
            rawNodeOutput.put(field.getKey(), field.getValue().getVal());
        }
        validateMandatoryFieldsFromCompletedEvent(userTaskFieldsGroupedByName.values(), rawNodeOutput.keySet());
        VariableValueModel output = new VariableValueModel();
        output.setType(VariableType.JSON_OBJ);
        output.setJsonObjVal(rawNodeOutput);

        getNodeRun().complete(output, new Date());
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
        return getDao().get(nodeRunId);
    }

    // TODO: LH-314
    public void processTaskSavedEvent() {
        throw new NotImplementedException();
    }

    public List<GetableIndex<? extends AbstractGetable<?>>> getIndexConfigurations() {
        return List.of(
                new GetableIndex<UserTaskRunModel>(
                        List.of(Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                // Future: We will make this LOCAL if it's DONE or CANCELLED, and
                // REMOTE if it's CLAIMED, UNASSIGNED, or ASSIGNED_NOT_CLAIMED.
                new GetableIndex<UserTaskRunModel>(
                        List.of(
                                Pair.of("status", GetableIndex.ValueType.SINGLE),
                                Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<UserTaskRunModel>(
                        List.of(Pair.of("status", GetableIndex.ValueType.SINGLE)), Optional.of(TagStorageType.LOCAL)),
                new GetableIndex<UserTaskRunModel>(
                        List.of(Pair.of("userId", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.REMOTE),
                        userTaskRun -> userTaskRun.getUserId() != null),
                new GetableIndex<UserTaskRunModel>(
                        List.of(
                                Pair.of("status", GetableIndex.ValueType.SINGLE),
                                Pair.of("userId", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL),
                        userTaskRun -> userTaskRun.getUserId() != null),
                new GetableIndex<UserTaskRunModel>(
                        List.of(
                                Pair.of("userId", GetableIndex.ValueType.SINGLE),
                                Pair.of("userGroup", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL),
                        userTaskRun -> userTaskRun.getUserId() != null && userTaskRun.getUserGroup() != null),
                new GetableIndex<UserTaskRunModel>(
                        List.of(
                                Pair.of("status", GetableIndex.ValueType.SINGLE),
                                Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE),
                                Pair.of("userId", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL),
                        userTaskRun -> userTaskRun.getUserId() != null),
                new GetableIndex<UserTaskRunModel>(
                        List.of(
                                Pair.of("status", GetableIndex.ValueType.SINGLE),
                                Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE),
                                Pair.of("userGroup", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL),
                        userTaskRun -> userTaskRun.getUserGroup() != null),
                new GetableIndex<UserTaskRunModel>(
                        List.of(
                                Pair.of("status", GetableIndex.ValueType.SINGLE),
                                Pair.of("userGroup", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL),
                        userTaskRun -> userTaskRun.getUserGroup() != null),
                new GetableIndex<UserTaskRunModel>(
                        List.of(Pair.of("userGroup", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.REMOTE),
                        userTaskRun -> userTaskRun.getUserGroup() != null));
    }

    public boolean isRemote() {
        return isRemote(this.getStatus());
    }

    public static boolean isRemote(UserTaskRunStatus status) {
        return (status == UserTaskRunStatus.ASSIGNED || status == UserTaskRunStatus.UNASSIGNED);
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        switch (key) {
            case "status" -> {
                return List.of(getIndexedStatusField(key, tagStorageType));
            }
            case "userTaskDefName" -> {
                return List.of(new IndexedField(
                        key, this.getUserTaskDefId().getName(), tagStorageType.get() // Is this right?
                        ));
            }
            case "userId" -> {
                return List.of(new IndexedField(key, this.getUserId(), TagStorageType.REMOTE));
            }
            case "userGroup" -> {
                return List.of(new IndexedField(key, this.getUserGroup(), tagStorageType.get()));
            }
        }
        log.warn("Tried to get value for unknown index field {}", key);
        return List.of();
    }

    private IndexedField getIndexedStatusField(String key, Optional<TagStorageType> tagStorageTypePbOptional) {
        TagStorageType tagStorageType = tagStorageTypePbOptional.get();
        if (this.isRemote()) {
            tagStorageType = TagStorageType.REMOTE;
        }
        return new IndexedField(key, this.getStatus().toString(), tagStorageType);
    }
}
