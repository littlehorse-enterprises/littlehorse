package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.AssignUserTaskRunRequestModel;
import io.littlehorse.common.model.command.subcommand.CompleteUserTaskRunRequestModel;
import io.littlehorse.common.model.command.subcommand.ReassignUserTask;
import io.littlehorse.common.model.meta.NodeModel;
import io.littlehorse.common.model.meta.UserTaskNodeModel;
import io.littlehorse.common.model.meta.usertasks.UTActionTriggerModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskFieldModel;
import io.littlehorse.common.model.objectId.NodeRunIdModel;
import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.wfrun.usertaskevent.UTEReassignedModel;
import io.littlehorse.common.model.wfrun.usertaskevent.UserTaskEventModel;
import io.littlehorse.common.proto.ReassignedUserTaskPb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskEvent;
import io.littlehorse.sdk.common.proto.UserTaskFieldResult;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class UserTaskRunModel extends Getable<UserTaskRun> {

    private UserTaskRunIdModel id;
    private UserTaskDefIdModel userTaskDefId;

    private List<UserTaskEventModel> events = new ArrayList<>();

    private UserTaskRun.OwnerCase ownerCase;
    private UserModel user;
    private UserGroupModel userGroup;
    private UserTaskNodeModel userTaskNode;
    private List<UserTaskFieldResult> results = new ArrayList<>();

    private UserTaskRunStatus status;
    private String notes;
    private Date scheduledTime;

    // If we ever allow ad-hoc User Tasks, this will move to an optional
    // field, or a `oneof user_task_source` field. However, note that such
    // a change would be fine from the API Compatibility perspective.
    private NodeRunIdModel nodeRunId;

    public UserTaskRunModel() {}

    public UserTaskRunModel(UserTaskDefModel utd, UserTaskNodeModel userTaskNode, NodeRunModel nodeRunModel) {
        this.userTaskDefId = utd.getObjectId();
        this.nodeRunId = nodeRunModel.getObjectId();
        this.id = new UserTaskRunIdModel(nodeRunId.getWfRunId());
        this.scheduledTime = new Date();
        this.userTaskNode = userTaskNode;
        ownerCase = switch (userTaskNode.getAssignmentType()) {
            case USER -> UserTaskRun.OwnerCase.USER;
            case USER_GROUP -> UserTaskRun.OwnerCase.USER_GROUP;
            default -> throw new IllegalArgumentException("Assignment Type not supported");};
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

        if (ownerCase.equals(UserTaskRun.OwnerCase.USER)) {
            if (user != null) out.setUser(user.toProto());
        } else if (ownerCase.equals(UserTaskRun.OwnerCase.USER_GROUP)) {
            if (userGroup != null) out.setUserGroup(userGroup.toProto());
        } else {
            throw new IllegalArgumentException("Owner case not supported yet");
        }

        if (notes != null) out.setNotes(notes);

        for (UserTaskEventModel event : events) {
            out.addEvents(event.toProto());
        }
        for (UserTaskFieldResult result : results) {
            out.addResults(result);
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
        ownerCase = p.getOwnerCase();
        if (ownerCase.equals(UserTaskRun.OwnerCase.USER)) {
            user = LHSerializable.fromProto(p.getUser(), UserModel.class);
        } else if (ownerCase.equals(UserTaskRun.OwnerCase.USER_GROUP)) {
            userGroup = LHSerializable.fromProto(p.getUserGroup(), UserGroupModel.class);
        } else {
            throw new IllegalArgumentException("Owner case not supported yet");
        }

        if (p.hasNotes()) notes = p.getNotes();

        for (UserTaskEvent ute : p.getEventsList()) {
            events.add(LHSerializable.fromProto(ute, UserTaskEventModel.class));
        }
        results.addAll(p.getResultsList());

        ownerCase = p.getOwnerCase();
    }

    public Date getCreatedAt() {
        return scheduledTime;
    }

    public boolean advanceIfPossible(Date time) {
        // User Tasks currently only care about the input from the workers, not the
        // other threads.
        return false;
    }

    public void onArrival(Date time) {
        NodeModel node = getNodeRun().getNode();
        getNodeRun().status = LHStatus.RUNNING;
        status = UserTaskRunStatus.UNASSIGNED;

        // Need to either assign to a user or to a group.
        try {
            if (node.userTaskNode.getNotes() != null) {
                VariableValueModel notesVal = getNodeRun()
                        .getThreadRun()
                        .assignVariable(node.userTaskNode.getNotes())
                        .asStr();

                notes = notesVal.getStrVal();
            }

            if (ownerCase == UserTaskRun.OwnerCase.USER) {
                assignToSpecificUser(node);
            } else if (ownerCase == UserTaskRun.OwnerCase.USER_GROUP) {
                assignToGroup(node);
            } else {
                status = UserTaskRunStatus.UNASSIGNED;
            }

            // I don't think there's anything to do other than schedule the timers for
            // the actions which need to occur.
            for (UTActionTriggerModel action : node.userTaskNode.getActions(UTHook.ON_ARRIVAL)) {
                scheduleAction(action);
            }
            log.info("Arrived at user task!");
        } catch (LHVarSubError exn) {
            // darnit ):
            getNodeRun()
                    .fail(
                            new FailureModel(
                                    "Invalid variables when creating UserTaskRun: " + exn.getMessage(),
                                    LHConstants.VAR_SUB_ERROR),
                            time);
        }
    }

    private void assignToSpecificUser(NodeModel node) throws LHVarSubError {
        ThreadRunModel threadRunModel = getNodeRun().getThreadRun();
        VariableValueModel userIdVal =
                threadRunModel.assignVariable(node.userTaskNode.getUser().getUserId());
        VariableValueModel userGroupVal = node.userTaskNode.getUser().getUserGroup() != null
                ? threadRunModel.assignVariable(node.userTaskNode.getUser().getUserGroup())
                : null;
        if (userIdVal.type != VariableType.STR) {
            throw new LHVarSubError(
                    null,
                    "VariableAssignment for specific user id should be STR!" + " What we got is: " + userIdVal.type);
        }
        if (userGroupVal != null) {
            user = new UserModel(userIdVal.strVal, new UserGroupModel(userGroupVal.strVal));
        } else {
            user = new UserModel(userIdVal.strVal);
        }
        status = UserTaskRunStatus.ASSIGNED;

        // now add Audit Log Event
        UTEReassignedModel reassigned = new UTEReassignedModel();
        reassigned.setNewUser(user);
        events.add(new UserTaskEventModel(reassigned, new Date()));
    }

    private void assignToGroup(NodeModel node) throws LHVarSubError {
        VariableValueModel groupIdVal = getNodeRun().getThreadRun().assignVariable(node.userTaskNode.getUserGroup());

        if (groupIdVal.type != VariableType.STR) {
            throw new LHVarSubError(
                    null, "VariableAssignment for group id should be STR!" + " What we got is: " + groupIdVal.type);
        }
        userGroup = new UserGroupModel(groupIdVal.strVal);
        status = UserTaskRunStatus.UNASSIGNED;

        // now add Audit Log Event
        UTEReassignedModel reassigned = new UTEReassignedModel();
        reassigned.setNewUserGroup(userGroup);
        events.add(new UserTaskEventModel(reassigned, new Date()));
    }

    private void scheduleAction(UTActionTriggerModel trigger) throws LHVarSubError {
        trigger.schedule(getNodeRun().getThreadRun().wfRunModel.getDao(), this);
    }

    public UserGroupModel getUserGroup() {
        if (user != null && user.getUserGroup() != null) {
            return user.getUserGroup();
        } else {
            return userGroup;
        }
    }

    public void reassignTo(AssignUserTaskRunRequestModel event) {
        UTEReassignedModel reassigned = null;
        switch (event.getAssigneeType()) {
            case USER:
                UserModel user = new UserModel(event.getUser().getId(), this.getUserGroup());
                reassigned = reassignToUser(user, true);
                break;
            case USER_GROUP:
                reassigned = reassignToUserGroup(event.getUserGroup());
                break;
            case ASSIGNEE_NOT_SET:
        }
        if (reassigned != null) {
            events.add(new UserTaskEventModel(reassigned, new Date()));
        }
    }

    public void deadlineReassign(String newOwner, ReassignedUserTaskPb.AssignToCase assignToCase) {
        UTEReassignedModel reassigned = null;
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

    private UTEReassignedModel reassignToUserGroup(UserGroupModel newUserGroup) {
        UTEReassignedModel ute = new UTEReassignedModel();
        ute.setNewUserGroup(newUserGroup);
        ute.setOldUserGroup(userGroup);
        ute.setNewUser(null);
        ute.setOldUser(user);

        ownerCase = UserTaskRun.OwnerCase.USER_GROUP;
        userGroup = newUserGroup;
        user = null;
        status = UserTaskRunStatus.UNASSIGNED;
        return ute;
    }

    private UTEReassignedModel reassignToUser(UserModel newUser, boolean triggerAction) {
        UTEReassignedModel ute = new UTEReassignedModel();
        ute.setNewUser(newUser);
        ute.setOldUser(user);
        ownerCase = UserTaskRun.OwnerCase.USER;
        user = newUser;
        status = UserTaskRunStatus.ASSIGNED;
        NodeModel node = getNodeRun().getNode();
        if (triggerAction) {
            for (UTActionTriggerModel action : node.getUserTaskNode().getActions(UTHook.ON_TASK_ASSIGNED)) {
                scheduleTaskReassign(action);
            }
        }
        return ute;
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
        ReassignedUserTaskPb.AssignToCase assignToCase = null;
        switch (action.getReassign().getAssignToCase()) {
            case USER_ID:
                assignToCase = ReassignedUserTaskPb.AssignToCase.USER_ID;
                break;
            case USER_GROUP:
                assignToCase = ReassignedUserTaskPb.AssignToCase.USER_GROUP;
                break;
        }
        LHTimer timer = new LHTimer(
                new Command(
                        new ReassignUserTask(
                                getNodeRun().getObjectId(),
                                action.getReassign()
                                        .getNewOwner()
                                        .getRhsLiteralValue()
                                        .getStrVal(),
                                assignToCase),
                        maturationTime),
                getDao());
        getDao().scheduleTimer(timer);
    }

    public void processTaskCompletedEvent(CompleteUserTaskRunRequestModel event) throws LHValidationError {
        if (getNodeRun().getStatus() != LHStatus.STARTING && getNodeRun().getStatus() != LHStatus.RUNNING) {
            log.warn("Tried to complete a user task that was not running");
            return;
        }

        user = new UserModel(event.getUserId());
        status = UserTaskRunStatus.DONE;

        // Now we need to create an output thing...
        // TODO LH-309: Validate this vs the schema
        Map<String, Object> raw = new HashMap<>();
        UserTaskDefModel userTaskDef = getDao().getUserTaskDef(
                        getUserTaskDefId().getName(), getUserTaskDefId().getVersion());
        Map<String, UserTaskFieldModel> userTaskFieldsGroupedByName = userTaskDef.getFields().stream()
                .collect(Collectors.toMap(UserTaskFieldModel::getName, Function.identity()));
        for (UserTaskFieldResult inputField : event.getResult().getFieldsList()) {
            UserTaskFieldModel userTaskFieldFromTaskDef = userTaskFieldsGroupedByName.get(inputField.getName());
            boolean isUndefined = userTaskFieldFromTaskDef == null
                    || !userTaskFieldFromTaskDef
                            .getType()
                            .equals(inputField.getValue().getType());
            if (isUndefined) {
                throw new LHValidationError("Field [name = %s, type = %s] is not defined in UserTask schema"
                        .formatted(inputField.getName(), inputField.getValue().getType()));
            }
            results.add(inputField);
            VariableValueModel fieldVal = VariableValueModel.fromProto(inputField.getValue());
            raw.put(inputField.getName(), fieldVal.getVal());
        }
        validateMandatoryFieldsFromCompletedEvent(userTaskFieldsGroupedByName.values(), raw.keySet());
        VariableValueModel output = new VariableValueModel();
        output.setType(VariableType.JSON_OBJ);
        output.setJsonObjVal(raw);

        getNodeRun().complete(output, new Date());
    }

    private void validateMandatoryFieldsFromCompletedEvent(
            Collection<UserTaskFieldModel> userTaskFieldsFromTaskDef, Collection<String> inputFieldNames)
            throws LHValidationError {
        List<String> mandatoryFieldNames = userTaskFieldsFromTaskDef.stream()
                .filter(UserTaskFieldModel::isRequired)
                .map(UserTaskFieldModel::getName)
                .toList();
        String mandatoryFieldsNotFound = mandatoryFieldNames.stream()
                .filter(Predicate.not(inputFieldNames::contains))
                .collect(Collectors.joining(","));
        if (!mandatoryFieldsNotFound.isEmpty()) {
            throw new LHValidationError("[%s] are mandatory fields".formatted(mandatoryFieldsNotFound));
        }
    }

    public NodeRunModel getNodeRun() {
        return getDao().getNodeRun(nodeRunId);
    }

    // TODO: LH-314
    public void processTaskSavedEvent() {
        throw new NotImplementedException();
    }

    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
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
                        userTaskRun -> userTaskRun.getUser() != null),
                new GetableIndex<UserTaskRunModel>(
                        List.of(
                                Pair.of("status", GetableIndex.ValueType.SINGLE),
                                Pair.of("userId", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL),
                        userTaskRun -> userTaskRun.getUser() != null),
                new GetableIndex<UserTaskRunModel>(
                        List.of(
                                Pair.of("userId", GetableIndex.ValueType.SINGLE),
                                Pair.of("userGroup", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL),
                        userTaskRun -> userTaskRun.getUser() != null
                                && userTaskRun.getUser().getUserGroup() != null),
                new GetableIndex<UserTaskRunModel>(
                        List.of(
                                Pair.of("status", GetableIndex.ValueType.SINGLE),
                                Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE),
                                Pair.of("userId", GetableIndex.ValueType.SINGLE)),
                        Optional.of(TagStorageType.LOCAL),
                        userTaskRun -> userTaskRun.getUser() != null),
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

    public UserTaskTriggerContextModel buildTaskContext() {
        return new UserTaskTriggerContextModel(user, userGroup);
    }

    private VarNameAndValModel getVarNameAndValue(String varName, String varValue) {
        VariableValueModel variableValue = new VariableValueModel(varValue);
        return new VarNameAndValModel(varName, variableValue);
    }

    public static boolean isRemote(UserTaskRunStatus UserTaskRunStatus) {
        return (UserTaskRunStatus == UserTaskRunStatus.ASSIGNED || UserTaskRunStatus == UserTaskRunStatus.UNASSIGNED);
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
                return List.of(new IndexedField(key, this.getUser().getId(), TagStorageType.REMOTE));
            }
            case "userGroup" -> {
                return List.of(new IndexedField(key, this.getUserGroup().getId(), tagStorageType.get()));
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
