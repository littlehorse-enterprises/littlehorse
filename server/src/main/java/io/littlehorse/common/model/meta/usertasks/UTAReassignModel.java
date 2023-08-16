package io.littlehorse.common.model.meta.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.VariableAssignmentModel;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTAReassign;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UTAReassignModel extends LHSerializable<UTAReassign> {

    private UTAReassign.AssignToCase assignToCase;
    private VariableAssignmentModel newOwner;

    @Override
    public UTAReassign.Builder toProto() {
        UTAReassign.Builder reassignBuilder = UTAReassign.newBuilder();
        if (assignToCase == UTAReassign.AssignToCase.USER_ID) {
            reassignBuilder.setUserId(newOwner.toProto());
        } else if (assignToCase.equals(UTAReassign.AssignToCase.USER_GROUP)) {
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
        UTAReassign reassignPb = (UTAReassign) proto;
        this.assignToCase = reassignPb.getAssignToCase();
        if (assignToCase.equals(UTAReassign.AssignToCase.USER_ID)) {
            this.newOwner = VariableAssignmentModel.fromProto(reassignPb.getUserId());
        } else if (assignToCase.equals(UTAReassign.AssignToCase.USER_GROUP)) {
            this.newOwner = VariableAssignmentModel.fromProto(reassignPb.getUserGroup());
        } else {
            throw new LHSerdeError(
                null,
                "Assign operation not supported yet " + assignToCase
            );
        }
    }

    @Override
    public Class<UTAReassign> getProtoBaseClass() {
        return UTAReassign.class;
    }
}
