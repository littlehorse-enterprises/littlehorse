package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommented;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UTECommentEditedModel extends LHSerializable<UTECommented> {

    private Integer userCommentId;
    private String userId;
    private String comment;

    @Override
    public UTECommented.Builder toProto() {
        UTECommented.Builder out = UTECommented.newBuilder();
        if (userCommentId != null) out.setUserCommentId(userCommentId);
        if (userId != null) out.setUserId(userId);
        if (comment != null) out.setComment(comment);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        UTECommented p = (UTECommented) proto;
        userCommentId = p.getUserCommentId();
        userId = p.getUserId();
        comment = p.getComment();
    }

    @Override
    public Class<UTECommented> getProtoBaseClass() {
        return UTECommented.class;
    }
}
