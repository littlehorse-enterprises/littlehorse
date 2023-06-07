package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.subcommand.CompleteUserTaskRun;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.UserTaskAction;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.UserTaskEvent;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.UserGroupsPb;
import io.littlehorse.jlib.common.proto.UserTaskEventPb;
import io.littlehorse.jlib.common.proto.UserTaskFieldResultPb;
import io.littlehorse.jlib.common.proto.UserTaskRunPb;
import io.littlehorse.jlib.common.proto.UserTaskRunPb.AssignedToCase;
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
    public List<UserTaskEvent> events;

    public AssignedToCase assignedToType;
    public String specificUserId;
    public UserGroupsPb groups;

    public String userId;
    public List<UserTaskFieldResultPb> results;

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
            .setUserTaskDefName(userTaskDefName);

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
            case GROUPS:
                out.setGroups(groups);
                break;
            case ASSIGNEDTO_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        return out;
    }

    public void initFrom(Message proto) {
        UserTaskRunPb p = (UserTaskRunPb) proto;
        userTaskDefName = p.getUserTaskDefName();

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
            case GROUPS:
                groups = p.getGroups();
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

        // Need to either assign to a user or to a group.
        try {
            if (assignedToType == AssignedToCase.SPECIFIC_USER_ID) {
                assignToSpecificUser(node);
            } else if (assignedToType == AssignedToCase.GROUPS) {
                assignToGroup(node);
            } else {
                // not possible
            }

            // I don't think there's anything to do other than schedule the timers for
            // the actions which need to occur.
            scheduleActions(time, node);
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

        // TODO: LH-311 update the UserTaskStatus
        userId = specificUserId;
    }

    private void assignToGroup(Node node) throws LHVarSubError {
        // TODO: LH-317
        throw new NotImplementedException();
    }

    private void scheduleActions(Date time, Node node) {
        for (UserTaskAction action : node.userTaskNode.actions) {
            log.warn(
                "Got an action " +
                action.task +
                ", but scheduling actions is not implemented"
            );
        }
    }

    // Technically this should live in the `streamsimpl` directory, as the `Tag`
    // is specific to the Kafka Streams Backend, but it doesn't look like we'll be
    // using any other backend for a while, so this can live here for now.
    //
    // That's why the other GETable's don't have a getTags(); rather, we use
    // TagUtils.java, which lives in streamsimpl.
    public List<Tag> getTags() {
        List<Tag> out = new ArrayList<>();

        // This will change as we add a UserTaskStatus enum; in particular, we will
        // add a `status` field and make tasks searchable by that (see LH-311)

        // Find all TODO's by User Id
        if (userId != null && nodeRun.status == LHStatusPb.RUNNING) {
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("userId", userId),
                    Pair.of("status", "TODO")
                )
            );
        }

        // Find all TODO's by User Id and Task Type
        if (userId != null && nodeRun.status == LHStatusPb.RUNNING) {
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("userId", userId),
                    Pair.of("userTaskDefName", userTaskDefName),
                    Pair.of("status", "TODO")
                )
            );
        }

        // Find all TODO's that are unclaimed
        if (userId == null && nodeRun.status == LHStatusPb.RUNNING) {
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("unclaimed", "true"),
                    Pair.of("status", "TODO")
                )
            );
        }

        // Find all TODO's that are unclaimed by Task Type
        if (userId == null && nodeRun.status == LHStatusPb.RUNNING) {
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("unclaimed", "true"),
                    Pair.of("userTaskDefName", userTaskDefName),
                    Pair.of("status", "TODO")
                )
            );
        }

        // Find COMPLETED by User Id
        if (nodeRun.status == LHStatusPb.COMPLETED) {
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("completed", "true"),
                    Pair.of("userId", userId)
                )
            );
        }

        // Find COMPLETED by User Id and Task Type
        if (nodeRun.status == LHStatusPb.COMPLETED) {
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("completed", "true"),
                    Pair.of("userId", userId),
                    Pair.of("userTaskDefName", userTaskDefName)
                )
            );
        }

        // Find COMPLETED by Task Type
        if (nodeRun.status == LHStatusPb.COMPLETED) {
            out.add(
                new Tag(
                    nodeRun,
                    TagStorageTypePb.LOCAL_UNCOUNTED,
                    Pair.of("completed", "true"),
                    Pair.of("userTaskDefName", userTaskDefName)
                )
            );
        }

        // TODO LH-317: Add support for searching by assigned group

        // TODO LH-318: Add ability to find cancelled/timed out tasks

        return out;
    }

    // TODO: LH-307
    public void processTaskAssignedEvent() {
        throw new NotImplementedException();
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

        // Now we need to create an output thing...
        // TODO LH-309: Validate this vs the schema
        Map<String, Object> raw = new HashMap<>();
        for (UserTaskFieldResultPb field : event.result.getFieldsList()) {
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
