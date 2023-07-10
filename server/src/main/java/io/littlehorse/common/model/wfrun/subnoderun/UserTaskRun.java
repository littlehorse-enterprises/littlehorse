package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.subcommand.AssignUserTaskRun;
import io.littlehorse.common.model.command.subcommand.CompleteUserTaskRun;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.usertasks.UTActionTrigger;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.model.wfrun.usertaskevent.UTEReassigned;
import io.littlehorse.common.model.wfrun.usertaskevent.UserTaskEvent;
import io.littlehorse.sdk.common.proto.LHStatusPb;
import io.littlehorse.sdk.common.proto.UserTaskEventPb;
import io.littlehorse.sdk.common.proto.UserTaskFieldResultPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb.AssignedToCase;
import io.littlehorse.sdk.common.proto.UserTaskRunStatusPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

@Slf4j
@Getter
@Setter
public class UserTaskRun extends SubNodeRun<UserTaskRunPb> {

    public String userTaskDefName;
    public int userTaskDefVersion;
    public List<UserTaskEvent> events;

    public AssignedToCase assignedToType;
    public String specificUserId;
    public String userGroup;

    public String userId;
    public List<UserTaskFieldResultPb> results;

    public UserTaskRunStatusPb status;
    private String notes;

    public UserTaskRun() {
        events = new ArrayList<>();
        results = new ArrayList<>();
    }

    public Class<UserTaskRunPb> getProtoBaseClass() {
        return UserTaskRunPb.class;
    }

    public UserTaskRunPb.Builder toProto() {
        UserTaskRunPb.Builder out = UserTaskRunPb
            .newBuilder()
            .setUserTaskDefName(userTaskDefName)
            .setStatus(status)
            .setUserTaskDefVersion(userTaskDefVersion);

        if (userId != null) out.setUserId(userId);

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

    public void initFrom(Message proto) {
        UserTaskRunPb p = (UserTaskRunPb) proto;
        userTaskDefName = p.getUserTaskDefName();
        userTaskDefVersion = p.getUserTaskDefVersion();
        status = p.getStatus();

        if (p.hasUserId()) userId = p.getUserId();

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

    public boolean advanceIfPossible(Date time) {
        // User Tasks currently only care about the input from the workers, not the
        // other threads.
        return false;
    }

    public void arrive(Date time) {
        Node node = nodeRun.getNode();
        nodeRun.status = LHStatusPb.RUNNING;
        status = UserTaskRunStatusPb.UNASSIGNED;

        // Need to either assign to a user or to a group.
        try {
            if (node.userTaskNode.getNotes() != null) {
                VariableValue notesVal = nodeRun
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
            for (UTActionTrigger action : node.userTaskNode.getActions()) {
                scheduleAction(action);
            }
            log.info("Arrived at user task!");
        } catch (LHVarSubError exn) {
            // darnit ):
            nodeRun.fail(
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
        VariableValue userIdVal = nodeRun
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
        userId = specificUserId;

        // now add Audit Log Event
        UTEReassigned reassigned = new UTEReassigned();
        reassigned.setNewUserId(specificUserId);
        events.add(new UserTaskEvent(reassigned, new Date()));
    }

    private void assignToGroup(Node node) throws LHVarSubError {
        VariableValue groupIdVal = nodeRun
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
        userId = null;

        // now add Audit Log Event
        UTEReassigned reassigned = new UTEReassigned();
        reassigned.setNewUserGroup(userGroup);
        events.add(new UserTaskEvent(reassigned, new Date()));
    }

    private void scheduleAction(UTActionTrigger trigger) throws LHVarSubError {
        trigger.schedule(nodeRun.getThreadRun().wfRun.getDao(), this);
    }

    public void reassignTo(AssignUserTaskRun event) {
        UTEReassigned ute = new UTEReassigned();
        switch (event.assigneeType) {
            case USER_GROUP:
                ute.setNewUserGroup(event.userGroup);
                ute.setOldUserGroup(userGroup);
                ute.setNewUserId(null);
                ute.setOldUserId(userId);

                assignedToType = AssignedToCase.USER_GROUP;
                userGroup = event.userGroup;
                specificUserId = null;
                userId = null;
                status = UserTaskRunStatusPb.ASSIGNED_NOT_CLAIMED;
                break;
            case USER_ID:
                ute.setNewUserId(event.userId);
                ute.setOldUserId(userId);

                assignedToType = AssignedToCase.SPECIFIC_USER_ID;
                userId = event.userId;
                specificUserId = event.userId;
                status = UserTaskRunStatusPb.CLAIMED;
                break;
            case ASSIGNEE_NOT_SET:
            // nothing to do, this isn't possible.
        }

        events.add(new UserTaskEvent(ute, new Date()));
    }

    public void processTaskCompletedEvent(CompleteUserTaskRun event) {
        if (
            nodeRun.status != LHStatusPb.STARTING &&
            nodeRun.status != LHStatusPb.RUNNING
        ) {
            // TODO LH-303: throw error back to client.
            log.warn("Tried to complete a user task that was not running");
            return;
        }

        userId = event.userId;
        status = UserTaskRunStatusPb.DONE;

        // Now we need to create an output thing...
        // TODO LH-309: Validate this vs the schema
        Map<String, Object> raw = new HashMap<>();
        for (UserTaskFieldResultPb field : event.result.getFieldsList()) {
            results.add(field);
            VariableValue fieldVal = VariableValue.fromProto(field.getValue());
            raw.put(field.getName(), fieldVal.getVal());
        }

        VariableValue output = new VariableValue();
        output.type = VariableTypePb.JSON_OBJ;
        output.jsonObjVal = raw;

        nodeRun.complete(output, new Date());
    }

    // TODO: LH-314
    public void processTaskSavedEvent() {
        throw new NotImplementedException();
    }
}
