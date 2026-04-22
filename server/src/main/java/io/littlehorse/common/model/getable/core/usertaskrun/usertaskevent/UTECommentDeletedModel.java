package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommentDeleted;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class UTECommentDeletedModel extends LHSerializable<UTECommentDeleted> {

    private Integer userCommentId;
    private String userId;

    @Override
    public UTECommentDeleted.Builder toProto() {
        UTECommentDeleted.Builder out = UTECommentDeleted.newBuilder();
        if (userCommentId != null) out.setUserCommentId(userCommentId);
        if (userId != null) out.setUserId(userId);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        UTECommentDeleted p = (UTECommentDeleted) proto;
        userCommentId = p.getUserCommentId();
        userId = p.getUserId();
    }

    @Override
    public Class<UTECommentDeleted> getProtoBaseClass() {
        return UTECommentDeleted.class;
    }

    public Integer getUserCommentId() {
        return this.userCommentId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserCommentId(final Integer userCommentId) {
        this.userCommentId = userCommentId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public UTECommentDeletedModel() {}

    public UTECommentDeletedModel(final Integer userCommentId, final String userId) {
        this.userCommentId = userCommentId;
        this.userId = userId;
    }
}
