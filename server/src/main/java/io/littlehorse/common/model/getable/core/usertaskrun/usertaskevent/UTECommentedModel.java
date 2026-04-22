package io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.UserTaskEvent.UTECommented;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class UTECommentedModel extends LHSerializable<UTECommented> {

    private Integer userCommentId;
    private String userId;
    private String comment;

    public UTECommentedModel(String userId, String comment, Integer userCommentId) {
        this.userId = userId;
        this.comment = comment;
        this.userCommentId = userCommentId;
    }

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

    public Integer getUserCommentId() {
        return this.userCommentId;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getComment() {
        return this.comment;
    }

    public void setUserCommentId(final Integer userCommentId) {
        this.userCommentId = userCommentId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public UTECommentedModel() {}

    public UTECommentedModel(final Integer userCommentId, final String userId, final String comment) {
        this.userCommentId = userCommentId;
        this.userId = userId;
        this.comment = comment;
    }
}
