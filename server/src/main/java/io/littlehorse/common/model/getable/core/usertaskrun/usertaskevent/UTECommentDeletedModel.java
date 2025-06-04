package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommentDeleted;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UTECommentDeletedModel extends LHSerializable<UTECommentDeleted>{

  
    private Integer userCommentId ;  

    @Override
    public UTECommentDeleted.Builder toProto() {
        UTECommentDeleted.Builder out = UTECommentDeleted.newBuilder();
        if (userCommentId != null ) out.setUserCommentId(userCommentId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
           UTECommentDeleted p = (UTECommentDeleted) proto ;
           userCommentId = p.getUserCommentId() ;
    }

    @Override
    public Class<UTECommentDeleted> getProtoBaseClass() {
        return UTECommentDeleted.class ;
        
    }

}
