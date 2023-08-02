package io.littlehorse.common.model.wfrun;

import com.google.common.base.Strings;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommand.AssignUserTaskRun;
import io.littlehorse.common.model.command.subcommand.CompleteUserTaskRun;
import io.littlehorse.common.model.command.subcommand.ReassignedUserTask;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.subnode.UserTaskNode;
import io.littlehorse.common.model.meta.usertasks.UTActionTrigger;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.model.objectId.NodeRunId;
import io.littlehorse.common.model.objectId.UserTaskDefId;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.wfrun.usertaskevent.UTEReassigned;
import io.littlehorse.common.model.wfrun.usertaskevent.UserTaskEvent;
import io.littlehorse.common.proto.ReassignedUserTaskPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb.UTHook;
import io.littlehorse.sdk.common.proto.UserTaskEventPb;
import io.littlehorse.sdk.common.proto.UserTaskFieldResultPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb.AssignedToCase;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private List<UserTaskEvent> events;

    private AssignedToCase assignedToType;
    private String specificUserId;
    private String userGroup;

    private String claimedByUserId;
    private List<UserTaskFieldResultPb> results;

    private UserTaskRunStatusPb status;
    private String notes;
    private Date scheduledTime;

    // If we ever allow ad-hoc User Tasks, this will move to an optional
    // field, or a `oneof user_task_source` field. However, note that such
    // a change would be fine from the API Compatibility perspective.
    private NodeRunId nodeRunId;

    public UserTaskRun() {
        events = new ArrayList<>();
        results = new ArrayList<>();
    }

    public UserTaskRun(UserTaskDef utd, UserTaskNode utn, NodeRun nodeRun) {
        this();
        this.userTaskDefId = utd.getObjectId();
        this.nodeRunId = nodeRun.getObjectId();
        this.id = new UserTaskRunId(nodeRunId.getWfRunId());
        this.scheduledTime = new Date();

        switch (utn.getAssignmentType()) {
            case USER_GROUP:
                assignedToType = AssignedToCase.USER_GROUP;
                break;
            case USER_ID:
                assignedToType = AssignedToCase.SPECIFIC_USER_ID;
                break;
            case ASSIGNMENT_NOT_SET:
            // not possible.
        }
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

        if (claimedByUserId != null) out.setClaimedByUserId(claimedByUserId);
        if (notes != null) out.setNotes(notes);

        for (UserTaskEvent event : events) {
            out.addEvents(event.toProto());
        }
        for (UserTaskFieldResultPb result : results) {
            out.addResults(result);
        }

        switch (assignedToType) {
            case SPECIFIC_USER_ID:
                if (specificUserId != null) out.setSpecificUserId(specificUserId);
                break;
            case USER_GROUP:
                if (userGroup != null) out.setUserGroup(userGroup);
                break;
            case ASSIGNEDTO_NOT_SET:
                throw new RuntimeException("Not possible");
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

        if (p.hasClaimedByUserId()) claimedByUserId = p.getClaimedByUserId();

        if (p.hasNotes()) notes = p.getNotes();

        for (UserTaskEventPb ute : p.getEventsList()) {
            events.add(LHSerializable.fromProto(ute, UserTaskEvent.class));
        }
        for (UserTaskFieldResultPb utfr : p.getResultsList()) {
            results.add(utfr);
        }

        assignedToType = p.getAssignedToCase();

        switch (assignedToType) {
            case SPECIFIC_USER_ID:
                specificUserId = p.getSpecificUserId();
                break;
            case USER_GROUP:
                userGroup = p.getUserGroup();
                break;
            case ASSIGNEDTO_NOT_SET:
                throw new RuntimeException("Not possible");
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

    public void onArrival(Date time) {
        Node node = getNodeRun().getNode();
        getNodeRun().status = LHStatusPb.RUNNING;
        status = UserTaskRunStatusPb.UNASSIGNED;

        // Need to either assign to a user or to a group.
        try {
            if (node.userTaskNode.getNotes() != null) {
                VariableValue notesVal = getNodeRun()
                    .getThreadRun()
                    .assignVariable(node.userTaskNode.getNotes())
                    .asStr();

                notes = notesVal.getStrVal();
            }

            if (assignedToType == AssignedToCase.SPECIFIC_USER_ID) {
                assignToSpecificUser(node);
            } else if (assignedToType == AssignedToCase.USER_GROUP) {
                assignToGroup(node);
            } else {
                status = UserTaskRunStatusPb.UNASSIGNED;
            }

            // I don't think there's anything to do other than schedule the timers for
            // the actions which need to occur.
            for (UTActionTrigger action : node.userTaskNode.getActions(
                UTHook.DO_ON_ARRIVAL
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

    private void assignToSpecificUser(Node node) throws LHVarSubError {
        VariableValue userIdVal = getNodeRun()
            .getThreadRun()
            .assignVariable(node.userTaskNode.getUserId());

        if (userIdVal.type != VariableTypePb.STR) {
            throw new LHVarSubError(
                null,
                "VariableAssignment for specific user id should be STR!" +
                " What we got is: " +
                userIdVal.type
            );
        }

        specificUserId = userIdVal.strVal;
        status = UserTaskRunStatusPb.CLAIMED;
        claimedByUserId = specificUserId;

        // now add Audit Log Event
        UTEReassigned reassigned = new UTEReassigned();
        reassigned.setNewUserId(specificUserId);
        events.add(new UserTaskEvent(reassigned, new Date()));
    }

    private void assignToGroup(Node node) throws LHVarSubError {
        VariableValue groupIdVal = getNodeRun()
            .getThreadRun()
            .assignVariable(node.userTaskNode.getUserGroup());

        if (groupIdVal.type != VariableTypePb.STR) {
            throw new LHVarSubError(
                null,
                "VariableAssignment for group id should be STR!" +
                " What we got is: " +
                groupIdVal.type
            );
        }

        userGroup = groupIdVal.strVal;
        status = UserTaskRunStatusPb.ASSIGNED_NOT_CLAIMED;
        specificUserId = null;
        claimedByUserId = null;

        // now add Audit Log Event
        UTEReassigned reassigned = new UTEReassigned();
        reassigned.setNewUserGroup(userGroup);
        events.add(new UserTaskEvent(reassigned, new Date()));
    }

    private void scheduleAction(UTActionTrigger trigger) throws LHVarSubError {
        trigger.schedule(getNodeRun().getThreadRun().wfRun.getDao(), this);
    }

    public void deadlineReassignment(AssignUserTaskRun event) {
        UTEReassigned reassigned = null;
        switch (event.getAssigneeType()) {
            case USER_GROUP:
                reassigned = reassignToUserGroup(event.getUserGroup());
                break;
            case USER_ID:
                reassigned = reassignToUser(event.getUserId(), true);
                break;
            case ASSIGNEE_NOT_SET:
        }
        if (reassigned != null) {
            events.add(new UserTaskEvent(reassigned, new Date()));
        }
    }

    public void deadlineReassignment(
        String newOwner,
        ReassignedUserTaskPb.AssignToCase assignToCase
    ) {
        UTEReassigned reassigned = null;
        switch (assignToCase) {
            case USER_ID:
                reassigned = reassignToUser(newOwner, false);
                break;
            case USER_GROUP:
                reassigned = reassignToUserGroup(newOwner);
            case ASSIGNTO_NOT_SET:
        }
        if (reassigned != null) {
            events.add(new UserTaskEvent(reassigned, new Date()));
        }
    }

    private UTEReassigned reassignToUserGroup(String newUserGroup) {
        UTEReassigned ute = new UTEReassigned();
        ute.setNewUserGroup(newUserGroup);
        ute.setOldUserGroup(userGroup);
        ute.setNewUserId(null);
        ute.setOldUserId(claimedByUserId);

        assignedToType = AssignedToCase.USER_GROUP;
        userGroup = newUserGroup;
        specificUserId = null;
        claimedByUserId = null;
        status = UserTaskRunStatusPb.ASSIGNED_NOT_CLAIMED;
        return ute;
    }

    private UTEReassigned reassignToUser(String newUserId, boolean triggerAction) {
        UTEReassigned ute = new UTEReassigned();
        ute.setNewUserId(newUserId);
        ute.setOldUserId(claimedByUserId);

        assignedToType = AssignedToCase.SPECIFIC_USER_ID;
        claimedByUserId = newUserId;
        specificUserId = newUserId;
        status = UserTaskRunStatusPb.CLAIMED;
        Node node = getNodeRun().getNode();
        if (triggerAction) {
            for (UTActionTrigger action : node
                .getUserTaskNode()
                .getActions(UTHook.DO_ON_TASK_ASSIGNED)) {
                scheduleTaskReassign(action);
            }
        }
        return ute;
    }

    private void scheduleTaskReassign(UTActionTrigger action) {
        long delayInSeconds = action.getDelaySeconds().getRhsLiteralValue().intVal;
        Date maturationTime = new Date(
            System.currentTimeMillis() + (1000 * delayInSeconds)
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
                new ReassignedUserTask(
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

    public void processTaskCompletedEvent(CompleteUserTaskRun event) {
        if (
            getNodeRun().getStatus() != LHStatusPb.STARTING &&
            getNodeRun().getStatus() != LHStatusPb.RUNNING
        ) {
            log.warn("Tried to complete a user task that was not running");
            return;
        }

        claimedByUserId = event.getUserId();
        status = UserTaskRunStatusPb.DONE;

        // Now we need to create an output thing...
        // TODO LH-309: Validate this vs the schema
        Map<String, Object> raw = new HashMap<>();
        for (UserTaskFieldResultPb field : event.getResult().getFieldsList()) {
            results.add(field);
            VariableValue fieldVal = VariableValue.fromProto(field.getValue());
            raw.put(field.getName(), fieldVal.getVal());
        }

        VariableValue output = new VariableValue();
        output.setType(VariableTypePb.JSON_OBJ);
        output.setJsonObjVal(raw);

        getNodeRun().complete(output, new Date());
    }

    public NodeRun getNodeRun() {
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
                userTaskRun ->
                    !Strings.isNullOrEmpty(userTaskRun.getClaimedByUserId())
            ),
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("userId", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL),
                userTaskRun ->
                    !Strings.isNullOrEmpty(userTaskRun.getClaimedByUserId())
            ),
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE),
                    Pair.of("userId", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL),
                userTaskRun ->
                    !Strings.isNullOrEmpty(userTaskRun.getClaimedByUserId())
            ),
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("userTaskDefName", GetableIndex.ValueType.SINGLE),
                    Pair.of("userGroup", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL),
                userTaskRun -> !Strings.isNullOrEmpty(userTaskRun.getUserGroup())
            ),
            new GetableIndex<UserTaskRun>(
                List.of(
                    Pair.of("status", GetableIndex.ValueType.SINGLE),
                    Pair.of("userGroup", GetableIndex.ValueType.SINGLE)
                ),
                Optional.of(TagStorageTypePb.LOCAL),
                userTaskRun -> !Strings.isNullOrEmpty(userTaskRun.getUserGroup())
            ),
            new GetableIndex<UserTaskRun>(
                List.of(Pair.of("userGroup", GetableIndex.ValueType.SINGLE)),
                Optional.of(TagStorageTypePb.REMOTE),
                userTaskRun -> !Strings.isNullOrEmpty(userTaskRun.getUserGroup())
            )
        );
    }

    public boolean isRemote() {
        return isRemote(this.getStatus());
    }

    public static boolean isRemote(UserTaskRunStatusPb userTaskRunStatusPb) {
        return (
            userTaskRunStatusPb == UserTaskRunStatusPb.CLAIMED ||
            userTaskRunStatusPb == UserTaskRunStatusPb.ASSIGNED_NOT_CLAIMED ||
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
                        this.getClaimedByUserId(),
                        TagStorageTypePb.REMOTE
                    )
                );
            }
            case "userGroup" -> {
                return List.of(
                    new IndexedField(key, this.getUserGroup(), tagStorageTypePb.get())
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
