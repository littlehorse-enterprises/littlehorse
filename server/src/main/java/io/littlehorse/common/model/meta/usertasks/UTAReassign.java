package io.littlehorse.common.model.meta.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb.UTAReassignPb;
import lombok.Getter;

@Getter
public class UTAReassign extends LHSerializable<UTAReassignPb> {

    private UTAReassignPb.AssignToCase assignToCase;
    private VariableAssignment newOwner;

    @Override
    public UTAReassignPb.Builder toProto() {
        UTAReassignPb.Builder reassignBuilder = UTAReassignPb.newBuilder();
        if (assignToCase == UTAReassignPb.AssignToCase.USER_ID) {
            reassignBuilder.setUserId(newOwner.toProto());
        } else if (assignToCase.equals(UTAReassignPb.AssignToCase.USER_GROUP)) {
            reassignBuilder.setUserGroup(newOwner.toProto());
        } else {
            throw new IllegalStateException(
                "Assign operation not supported yet " + assignToCase
            );
        }
        return reassignBuilder;
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        UTAReassignPb reassignPb = (UTAReassignPb) proto;
        this.assignToCase = reassignPb.getAssignToCase();
        if (assignToCase.equals(UTAReassignPb.AssignToCase.USER_ID)) {
            this.newOwner = VariableAssignment.fromProto(reassignPb.getUserId());
        } else if (assignToCase.equals(UTAReassignPb.AssignToCase.USER_GROUP)) {
            this.newOwner = VariableAssignment.fromProto(reassignPb.getUserGroup());
        } else {
            throw new LHSerdeError(
                null,
                "Assign operation not supported yet " + assignToCase
            );
        }
    }

    @Override
    public Class<UTAReassignPb> getProtoBaseClass() {
        return UTAReassignPb.class;
    }
}
