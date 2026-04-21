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

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof UTECommentDeletedModel)) return false;
        final UTECommentDeletedModel other = (UTECommentDeletedModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$userCommentId = this.getUserCommentId();
        final Object other$userCommentId = other.getUserCommentId();
        if (this$userCommentId == null ? other$userCommentId != null : !this$userCommentId.equals(other$userCommentId))
            return false;
        final Object this$userId = this.getUserId();
        final Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof UTECommentDeletedModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $userCommentId = this.getUserCommentId();
        result = result * PRIME + ($userCommentId == null ? 43 : $userCommentId.hashCode());
        final Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        return result;
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

    @Override
    public String toString() {
        return "UTECommentDeletedModel(userCommentId=" + this.getUserCommentId() + ", userId=" + this.getUserId() + ")";
    }

    public UTECommentDeletedModel() {}

    public UTECommentDeletedModel(final Integer userCommentId, final String userId) {
        this.userCommentId = userCommentId;
        this.userId = userId;
    }
}
