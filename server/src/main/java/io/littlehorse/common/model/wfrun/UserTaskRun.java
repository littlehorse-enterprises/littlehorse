package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.AssignUserTaskRun;
import io.littlehorse.common.model.command.subcommand.CompleteUserTaskRun;
import io.littlehorse.common.model.command.subcommand.ReassignUserTask;
import io.littlehorse.common.model.meta.NodeModel;
import io.littlehorse.common.model.meta.UserTaskNode;
import io.littlehorse.common.model.meta.usertasks.UTActionTrigger;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.model.meta.usertasks.UserTaskField;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.UserTaskDefId;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.wfrun.usertaskevent.UTEReassigned;
import io.littlehorse.common.model.wfrun.usertaskevent.UserTaskEvent;
import io.littlehorse.common.proto.ReassignedUserTaskPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskEventPb;
import io.littlehorse.sdk.common.proto.UserTaskFieldResultPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
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
public class UserTaskRun extends Getable<UserTaskRunPb> {

    private UserTaskRunId id;
    private UserTaskDefId userTaskDefId;

    private List<UserTaskEvent> events = new ArrayList<>();

    private UserTaskRunPb.OwnerCase ownerCase;
    private User user;
    private UserGroup userGroup;
    private UserTaskNode userTaskNode;
    private List<UserTaskFieldResultPb> results = new ArrayList<>();

    private UserTaskRunStatusPb status;
    private String notes;
    private Date scheduledTime;

    // If we ever allow ad-hoc User Tasks, this will move to an optional
    // field, or a `oneof user_task_source` field. However, note that such
    // a change would be fine from the API Compatibility perspective.
    private NodeRunId nodeRunId;

    public UserTaskRun() {}

    public UserTaskRun(
        UserTaskDef utd,
        UserTaskNode userTaskNode,
        NodeRunModel nodeRunModel
    ) {
        this.userTaskDefId = utd.getObjectId();
        this.nodeRunId = nodeRunModel.getObjectId();
        this.id = new UserTaskRunId(nodeRunId.getWfRunId());
        this.scheduledTime = new Date();
        this.userTaskNode = userTaskNode;
        ownerCase =
            switch (userTaskNode.getAssignmentType()) {
                case USER -> UserTaskRunPb.OwnerCase.USER;
                case USER_GROUP -> UserTaskRunPb.OwnerCase.USER_GROUP;
                default -> throw new IllegalArgumentException(
                    "Assignment Type not supported"
                );
            };
    }

    public Class<UserTaskRunPb> getProtoBaseClass() {
        return UserTaskRunPb.class;
    }

    public UserTaskRunPb.Builder toProto() {
        UserTaskRunPb.Builder out = UserTaskRunPb
            .newBuilder()
            .setStatus(status)
            .setId(id.toProto())
            .setUserTaskDefId(userTaskDefId.toProto())
            .setScheduledTime(LHUtil.fromDate(scheduledTime))
            .setNodeRunId(nodeRunId.toProto());

        if (ownerCase.equals(UserTaskRunPb.OwnerCase.USER)) {
            if (user != null) out.setUser(user.toProto());
        } else if (ownerCase.equals(UserTaskRunPb.OwnerCase.USER_GROUP)) {
            if (userGroup != null) out.setUserGroup(userGroup.toProto());
        } else {
            throw new IllegalArgumentException("Owner case not supported yet");
        }

        if (notes != null) out.setNotes(notes);

        for (UserTaskEvent event : events) {
            out.addEvents(event.toProto());
        }
        for (UserTaskFieldResultPb result : results) {
            out.addResults(result);
        }

        return out;
    }

    public UserTaskRunId getObjectId() {
        return id;
    }

    public void initFrom(Message proto) {
        UserTaskRunPb p = (UserTaskRunPb) proto;
        id = LHSerializable.fromProto(p.getId(), UserTaskRunId.class);
        userTaskDefId =
            LHSerializable.fromProto(p.getUserTaskDefId(), UserTaskDefId.class);
        status = p.getStatus();
        scheduledTime = LHLibUtil.fromProtoTs(p.getScheduledTime());
        nodeRunId = LHSerializable.fromProto(p.getNodeRunId(), NodeRunId.class);
        ownerCase = p.getOwnerCase();
        if (ownerCase.equals(UserTaskRunPb.OwnerCase.USER)) {
            user = LHSerializable.fromProto(p.getUser(), User.class);
        } else if (ownerCase.equals(UserTaskRunPb.OwnerCase.USER_GROUP)) {
            userGroup = LHSerializable.fromProto(p.getUserGroup(), UserGroup.class);
        } else {
            throw new IllegalArgumentException("Owner case not supported yet");
        }

        if (p.hasNotes()) notes = p.getNotes();

        for (UserTaskEventPb ute : p.getEventsList()) {
            events.add(LHSerializable.fromProto(ute, UserTaskEvent.class));
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
        status = UserTaskRunStatusPb.UNASSIGNED;

        // Need to either assign to a user or to a group.
        try {
            if (node.userTaskNode.getNotes() != null) {
                VariableValueModel notesVal = getNodeRun()
                    .getThreadRun()
                    .assignVariable(node.userTaskNode.getNotes())
                    .asStr();

                notes = notesVal.getStrVal();
            }

            if (ownerCase == UserTaskRunPb.OwnerCase.USER) {
                assignToSpecificUser(node);
            } else if (ownerCase == UserTaskRunPb.OwnerCase.USER_GROUP) {
                assignToGroup(node);
            } else {
                status = UserTaskRunStatusPb.UNASSIGNED;
            }

            // I don't think there's anything to do other than schedule the timers for
            // the actions which need to occur.
            for (UTActionTrigger action : node.userTaskNode.getActions(
                UTHook.ON_ARRIVAL
            )) {
                scheduleAction(action);
            }
            log.info("Arrived at user task!");
        } catch (LHVarSubError exn) {
            // darnit ):
            getNodeRun()
                .fail(
                    new Failure(
                        "Invalid variables when creating UserTaskRun: " +
                        exn.getMessage(),
                        LHConstants.VAR_SUB_ERROR
                    ),
                    time
                );
        }
    }

    private void assignToSpecificUser(NodeModel node) throws LHVarSubError {
        ThreadRunModel threadRunModel = getNodeRun().getThreadRun();
        VariableValueModel userIdVal = threadRunModel.assignVariable(
            node.userTaskNode.getUser().getUserId()
        );
        VariableValueModel userGroupVal = node.userTaskNode
                .getUser()
                .getUserGroup() !=
            null
            ? threadRunModel.assignVariable(
                node.userTaskNode.getUser().getUserGroup()
            )
            : null;
        if (userIdVal.type != VariableType.STR) {
            throw new LHVarSubError(
                null,
                "VariableAssignment for specific user id should be STR!" +
                " What we got is: " +
                userIdVal.type
            );
        }
        if (userGroupVal != null) {
            user = new User(userIdVal.strVal, new UserGroup(userGroupVal.strVal));
        } else {
            user = new User(userIdVal.strVal);
        }
        status = UserTaskRunStatusPb.ASSIGNED;

        // now add Audit Log Event
        UTEReassigned reassigned = new UTEReassigned();
        reassigned.setNewUser(user);
        events.add(new UserTaskEvent(reassigned, new Date()));
    }

    private void assignToGroup(NodeModel node) throws LHVarSubError {
        VariableValueModel groupIdVal = getNodeRun()
            .getThreadRun()
            .assignVariable(node.userTaskNode.getUserGroup());

        if (groupIdVal.type != VariableType.STR) {
            throw new LHVarSubError(
                null,
                "VariableAssignment for group id should be STR!" +
                " What we got is: " +
                groupIdVal.type
            );
        }
        userGroup = new UserGroup(groupIdVal.strVal);
        status = UserTaskRunStatusPb.UNASSIGNED;

        // now add Audit Log Event
        UTEReassigned reassigned = new UTEReassigned();
        reassigned.setNewUserGroup(userGroup);
        events.add(new UserTaskEvent(reassigned, new Date()));
    }

    private void scheduleAction(UTActionTrigger trigger) throws LHVarSubError {
        trigger.schedule(getNodeRun().getThreadRun().wfRunModel.getDao(), this);
    }

    public UserGroup getUserGroup() {
        if (user != null && user.getUserGroup() != null) {
            return user.getUserGroup();
        } else {
            return userGroup;
        }
    }

    public void reassignTo(AssignUserTaskRun event) {
        UTEReassigned reassigned = null;
        switch (event.getAssigneeType()) {
            case USER:
                User user = new User(event.getUser().getId(), this.getUserGroup());
                reassigned = reassignToUser(user, true);
                break;
            case USER_GROUP:
                reassigned = reassignToUserGroup(event.getUserGroup());
                break;
            case ASSIGNEE_NOT_SET:
        }
        if (reassigned != null) {
            events.add(new UserTaskEvent(reassigned, new Date()));
        }
    }

    public void deadlineReassign(
        String newOwner,
        ReassignedUserTaskPb.AssignToCase assignToCase
    ) {
        UTEReassigned reassigned = null;
        User user = new User(newOwner, this.getUserGroup());
        switch (assignToCase) {
            case USER_ID:
                reassigned = reassignToUser(user, false);
                break;
            case USER_GROUP:
                reassigned = reassignToUserGroup(new UserGroup(newOwner));
            case ASSIGNTO_NOT_SET:
        }
        if (reassigned != null) {
            events.add(new UserTaskEvent(reassigned, new Date()));
        }
    }

    private UTEReassigned reassignToUserGroup(UserGroup newUserGroup) {
        UTEReassigned ute = new UTEReassigned();
        ute.setNewUserGroup(newUserGroup);
        ute.setOldUserGroup(userGroup);
        ute.setNewUser(null);
        ute.setOldUser(user);

        ownerCase = UserTaskRunPb.OwnerCase.USER_GROUP;
        userGroup = newUserGroup;
        user = null;
        status = UserTaskRunStatusPb.UNASSIGNED;
        return ute;
    }

    private UTEReassigned reassignToUser(User newUser, boolean triggerAction) {
        UTEReassigned ute = new UTEReassigned();
        ute.setNewUser(newUser);
        ute.setOldUser(user);
        ownerCase = UserTaskRunPb.OwnerCase.USER;
        user = newUser;
        status = UserTaskRunStatusPb.ASSIGNED;
        NodeModel node = getNodeRun().getNode();
        if (triggerAction) {
            for (UTActionTrigger action : node
                .getUserTaskNode()
                .getActions(UTHook.ON_TASK_ASSIGNED)) {
                scheduleTaskReassign(action);
            }
        }
        return ute;
    }

    public void cancel() {
        status = UserTaskRunStatusPb.CANCELLED;
        Failure failure = new Failure(
            "User task cancelled",
            LHConstants.USER_TASK_CANCELLED
        );
        getNodeRun().fail(failure, new Date());
    }

    private void scheduleTaskReassign(UTActionTrigger action) {
        long delayInSeconds = action.getDelaySeconds().getRhsLiteralValue().intVal;
        LocalDateTime localDateTime = LocalDateTime.now().plusSeconds(delayInSeconds);
        Date maturationTime = Date.from(
            localDateTime.atZone(ZoneId.systemDefault()).toInstant()
        );
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
                    action
                        .getReassign()
                        .getNewOwner()
                        .getRhsLiteralValue()
                        .getStrVal(),
                    assignToCase
                ),
                maturationTime
            ),
            getDao()
        );
        getDao().scheduleTimer(timer);
    }

    public void processTaskCompletedEvent(CompleteUserTaskRun event)
        throws LHValidationError {
        if (
            getNodeRun().getStatus() != LHStatus.STARTING &&
            getNodeRun().getStatus() != LHStatus.RUNNING
        ) {
            log.warn("Tried to complete a user task that was not running");
            return;
        }

        user = new User(event.getUserId());
        status = UserTaskRunStatusPb.DONE;

        // Now we need to create an output thing...
        // TODO LH-309: Validate this vs the schema
        Map<String, Object> raw = new HashMap<>();
        UserTaskDef userTaskDef = getDao()
            .getUserTaskDef(
                getUserTaskDefId().getName(),
                getUserTaskDefId().getVersion()
            );
        Map<String, UserTaskField> userTaskFieldsGroupedByName = userTaskDef
            .getFields()
            .stream()
            .collect(Collectors.toMap(UserTaskField::getName, Function.identity()));
        for (UserTaskFieldResultPb inputField : event.getResult().getFieldsList()) {
            UserTaskField userTaskFieldFromTaskDef = userTaskFieldsGroupedByName.get(
                inputField.getName()
            );
            if (
                userTaskFieldFromTaskDef == null ||
                !userTaskFieldFromTaskDef
                    .getType()
                    .equals(inputField.getValue().getType())
            ) {
                throw new LHValidationError(
                    "Field [name = %s, type = %s] is not defined in UserTask schema".formatted(
                            inputField.getName(),
                            inputField.getValue().getType()
                        )
                );
            }
            results.add(inputField);
            VariableValueModel fieldVal = VariableValueModel.fromProto(
                inputField.getValue()
            );
            raw.put(inputField.getName(), fieldVal.getVal());
        }
        validateMandatoryFieldsFromCompletedEvent(
            userTaskFieldsGroupedByName.values(),
            raw.keySet()
        );
        VariableValueModel output = new VariableValueModel();
        output.setType(VariableType.JSON_OBJ);
        output.setJsonObjVal(raw);

        getNodeRun().complete(output, new Date());
    }

    private void validateMandatoryFieldsFromCompletedEvent(
        Collection<UserTaskField> userTaskFieldsFromTaskDef,
        Collection<String> inputFieldNames
    ) throws LHValidationError {
        List<String> mandatoryFieldNames = userTaskFieldsFromTaskDef
            .stream()
            .filter(UserTaskField::isRequired)
            .map(UserTaskField::getName)
            .toList();
        String mandatoryFieldsNotFound = mandatoryFieldNames
            .stream()
            .filter(Predicate.not(inputFieldNames::contains))
            .collect(Collectors.joining(","));
        if (!mandatoryFieldsNotFound.isEmpty()) {
            throw new LHValidationError(
                "[%s] are mandatory fields".formatted(mandatoryFieldsNotFound)
            );
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
            new GetableIndex<UserTaskRun>(
                List.of(Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageTypePb.LOCAL)
            ),
            // Future: We will make this LOCAL if it's DONE or CANCELLED, and
            // REMOTE if it's CLAIMED, UNASSIGNED, or ASSIGNED_NOT_CLAIMED.
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL)
            ),
            new GetableIndex<UserTaskRun>(
                List.of(Pair.of("status", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageTypePb.LOCAL)
            ),
            new GetableIndex<UserTaskRun>(
                List.of(Pair.of("userId", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageTypePb.REMOTE),
                userTaskRun -> userTaskRun.getUser() != null
            ),
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("userId", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL),
                userTaskRun -> userTaskRun.getUser() != null
            ),
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("userId", GetableIndex.ValueType.SINGLE),
                    Pair.of("userGroup", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL),
                userTaskRun ->
                    userTaskRun.getUser() != null &&
                    userTaskRun.getUser().getUserGroup() != null
            ),
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE),
                    Pair.of("userId", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL),
                userTaskRun -> userTaskRun.getUser() != null
            ),
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE),
                    Pair.of("userGroup", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL),
                userTaskRun -> userTaskRun.getUserGroup() != null
            ),
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("userGroup", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL),
                userTaskRun -> userTaskRun.getUserGroup() != null
            ),
            new GetableIndex<UserTaskRun>(
                List.of(Pair.of("userGroup", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageTypePb.REMOTE),
                userTaskRun -> userTaskRun.getUserGroup() != null
            )
        );
    }

    public boolean isRemote() {
        return isRemote(this.getStatus());
    }

    public UserTaskTriggerContext buildTaskContext() {
        return new UserTaskTriggerContext(user, userGroup);
    }

    private VarNameAndVal getVarNameAndValue(String varName, String varValue) {
        VariableValueModel variableValue = new VariableValueModel(varValue);
        return new VarNameAndVal(varName, variableValue);
    }

    public static boolean isRemote(UserTaskRunStatusPb userTaskRunStatusPb) {
        return (
            userTaskRunStatusPb == UserTaskRunStatusPb.ASSIGNED ||
            userTaskRunStatusPb == UserTaskRunStatusPb.UNASSIGNED
        );
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        switch (key) {
            case "status" -> {
                return List.of(getIndexedStatusField(key, tagStorageTypePb));
            }
            case "userTaskDefName" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.getUserTaskDefId().getName(),
                        tagStorageTypePb.get() // Is this right?
                    )
                );
            }
            case "userId" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.getUser().getId(),
                        TagStorageTypePb.REMOTE
                    )
                );
            }
            case "userGroup" -> {
                return List.of(
                    new IndexedField(
                        key,
                        this.getUserGroup().getId(),
                        tagStorageTypePb.get()
                    )
                );
            }
        }
        log.warn("Tried to get value for unknown index field {}", key);
        return List.of();
    }

    private IndexedField getIndexedStatusField(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePbOptional
    ) {
        TagStorageTypePb tagStorageTypePb = tagStorageTypePbOptional.get();
        if (this.isRemote()) {
            tagStorageTypePb = TagStorageTypePb.REMOTE;
        }
        return new IndexedField(key, this.getStatus().toString(), tagStorageTypePb);
    }
}
