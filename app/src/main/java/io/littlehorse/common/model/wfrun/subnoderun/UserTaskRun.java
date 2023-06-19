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
import io.littlehorse.common.model.wfrun.UserTaskEvent;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.UserTaskEventPb;
import io.littlehorse.jlib.common.proto.UserTaskFieldResultPb;
import io.littlehorse.jlib.common.proto.UserTaskRunPb;
import io.littlehorse.jlib.common.proto.UserTaskRunPb.AssignedToCase;
import io.littlehorse.jlib.common.proto.UserTaskRunStatusPb;
import io.littlehorse.jlib.common.proto.VariableTypePb;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
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

        for (UserTaskEvent event : events) {
            out.addEvents(event.toProto());
        }
        for (UserTaskFieldResultPb result : results) {
            out.addResults(result);
        }

        switch (assignedToType) {
            case SPECIFIC_USER_ID:
                out.setSpecificUserId(specificUserId);
                break;
            case USER_GROUP:
                out.setUserGroup(userGroup);
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
            if (assignedToType == AssignedToCase.SPECIFIC_USER_ID) {
                assignToSpecificUser(node);
            } else if (assignedToType == AssignedToCase.USER_GROUP) {
                assignToGroup(node);
            } else {
                // not yet implemented--case when assigning
            }

            // I don't think there's anything to do other than schedule the timers for
            // the actions which need to occur.
            for (UTActionTrigger action : node.userTaskNode.actions) {
                scheduleAction(action);
            }
            log.info("Arrived at user task!");
        } catch (LHVarSubError exn) {
            // darnit ):
            nodeRun.fail(
                new Failure(
                    TaskResultCodePb.VAR_SUB_ERROR,
                    "Invalid variables when creating UserTaskRun: " +
                    exn.getMessage(),
                    LHConstants.VAR_SUB_ERROR
                ),
                time
            );
        }
    }

    private void assignToSpecificUser(Node node) throws LHVarSubError {
        VariableValue userIdVal = nodeRun.threadRun.assignVariable(
            node.userTaskNode.userId
        );

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
    }

    private void assignToGroup(Node node) throws LHVarSubError {
        VariableValue groupIdVal = nodeRun.threadRun.assignVariable(
            node.userTaskNode.userGroup
        );

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
    }

    private void scheduleAction(UTActionTrigger trigger) throws LHVarSubError {
        trigger.schedule(nodeRun.threadRun.wfRun.cmdDao, this);
    }

    // Technically this should live in the `streamsimpl` directory, as the `Tag`
    // is specific to the Kafka Streams Backend, but it doesn't look like we'll be
    // using any other backend for a while, so this can live here for now.
    //
    // That's why the other GETable's don't have a getTags(); rather, we use
    // TagUtils.java, which lives in streamsimpl.
    public List<Tag> getTags() {
        List<Tag> out = new ArrayList<>();

        /*
         * There are four possible fields in each tag:
         * 1. status
         * 2. userTaskDefName
         * 3. userId
         * 4. userGroup
         *
         * They are always in that order. 3) and 4) are mutually exclusive.
         *
         * When searching, it is valid to provide any combination of the three tags, but
         * at least one must be set.
         */

        // Tag by status and User Task Def Name
        out.add(
            new Tag(
                nodeRun,
                TagStorageTypePb.LOCAL_UNCOUNTED,
                Pair.of("userTaskDefName", userTaskDefName)
            )
        );
        out.add(
            new Tag(
                nodeRun,
                TagStorageTypePb.LOCAL_UNCOUNTED,
                Pair.of("status", status.toString())
            )
        );
        out.add(
            new Tag(
                nodeRun,
                TagStorageTypePb.LOCAL_UNCOUNTED,
                Pair.of("userTaskDefName", userTaskDefName),
                Pair.of("status", status.toString())
            )
        );

        // Tag by user if claimed.
        // When REMOTE tags are supported, note that every tag here will be
        // a REMOTE tag since a single user can only execute so many tasks...
        if (userId != null) {
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("status", status.toString()),
                    Pair.of("userId", userId)
                )
            );
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("status", status.toString()),
                    Pair.of("userTaskDefName", userTaskDefName),
                    Pair.of("userId", userId)
                )
            );
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("userTaskDefName", userTaskDefName),
                    Pair.of("userId", userId)
                )
            );
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("userId", userId)
                )
            );
        }

        // TODO: Make it configurable on a per-group basis whether
        // a group is REMOTE or LOCAL tag? Some big groups should be LOCAL,
        // but small groups should be REMOTE.
        if (userGroup != null) {
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("status", status.toString()),
                    Pair.of("userGroup", userGroup)
                )
            );
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("status", status.toString()),
                    Pair.of("userTaskDefName", userTaskDefName),
                    Pair.of("userGroup", userGroup)
                )
            );
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("userTaskDefName", userTaskDefName),
                    Pair.of("userGroup", userGroup)
                )
            );
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("userGroup", userGroup)
                )
            );
        }
        // TODO LH-317: Improve support for searching by assigned group
        return out;
    }

    public void reassignTo(AssignUserTaskRun event) {
        switch (event.assigneeType) {
            case USER_GROUP:
                assignedToType = AssignedToCase.USER_GROUP;
                userGroup = event.userGroup;
                specificUserId = null;
                userId = null;
                status = UserTaskRunStatusPb.ASSIGNED_NOT_CLAIMED;
                break;
            case USER_ID:
                assignedToType = AssignedToCase.SPECIFIC_USER_ID;
                userId = event.userId;
                specificUserId = event.userId;
                status = UserTaskRunStatusPb.CLAIMED;
                break;
            case ASSIGNEE_NOT_SET:
            // nothing to do, this isn't possible.
        }
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
