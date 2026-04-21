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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UTECommentedModel)) return false;
        final UTECommentedModel other = (UTECommentedModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$userCommentId = this.getUserCommentId();
        final Object other$userCommentId = other.getUserCommentId();
        if (this$userCommentId == null ? other$userCommentId != null : !this$userCommentId.equals(other$userCommentId))
            return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final Object this$comment = this.getComment();
        final Object other$comment = other.getComment();
        if (this$comment == null ? other$comment != null : !this$comment.equals(other$comment)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UTECommentedModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $userCommentId = this.getUserCommentId();
        result = result * PRIME + ($userCommentId == null ? 43 : $userCommentId.hashCode());
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final Object $comment = this.getComment();
        result = result * PRIME + ($comment == null ? 43 : $comment.hashCode());
        return result;
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

    @Override
    public String toString() {
        return "UTECommentedModel(userCommentId=" + this.getUserCommentId() + ", userId=" + this.getUserId()
                + ", comment=" + this.getComment() + ")";
    }

    public UTECommentedModel() {}

    public UTECommentedModel(final Integer userCommentId, final String userId, final String comment) {
        this.userCommentId = userCommentId;
        this.userId = userId;
        this.comment = comment;
    }
}
