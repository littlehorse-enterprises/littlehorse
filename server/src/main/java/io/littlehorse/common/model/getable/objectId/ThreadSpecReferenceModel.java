package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ThreadSpecReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ThreadSpecReferenceModel extends LHSerializable<ThreadSpecReference> {

    private WfSpecIdModel wfSpecId;
    private Integer threadNumber;

    public ThreadSpecReferenceModel() {}

    public ThreadSpecReferenceModel(WfSpecIdModel wfSpecId, Integer threadNumber) {
        this.wfSpecId = wfSpecId;
        this.threadNumber = threadNumber;
    }

    public ThreadSpecReferenceModel(WfSpecIdModel wfSpecId) {
        this.wfSpecId = wfSpecId;
        this.threadNumber = null;
    }

    @Override
    public ThreadSpecReference.Builder toProto() {
        ThreadSpecReference.Builder builder = ThreadSpecReference.newBuilder();
        builder.setWfSpecId(wfSpecId.toProto());
        if (threadNumber != null) {
            builder.setThreadNumber(threadNumber);
        }
        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        ThreadSpecReference p = (ThreadSpecReference) proto;
        this.wfSpecId = LHSerializable.fromProto(p.getWfSpecId(), WfSpecIdModel.class, context);
        this.threadNumber = p.hasThreadNumber() ? p.getThreadNumber() : null;
    }

    @Override
    public Class<ThreadSpecReference> getProtoBaseClass() {
        return ThreadSpecReference.class;
    }

    @Override
    public String toString() {
        if (threadNumber != null) {
            return LHUtil.getCompositeId(wfSpecId.toString(), threadNumber.toString());
        } else {
            return LHUtil.getCompositeId(wfSpecId.toString());
        }
    }

    public void initFromKeyString(String keyString) {}
}
