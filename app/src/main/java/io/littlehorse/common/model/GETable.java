package io.littlehorse.common.model;

import java.util.Date;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.scheduler.WfRun;
import io.littlehorse.common.proto.WFRunPb;
import io.littlehorse.common.proto.WFSpecPb;

public abstract class GETable {
    public String id;
    public Date createdAt;

    public abstract MessageOrBuilder toProtoBuilder();

    public static void foo() {}

    @SuppressWarnings("unchecked")
    public static <T extends GETable> T fromProtoBytes(byte[] b, Class<T> cls)
    throws InvalidProtocolBufferException {
        if (cls == WfRun.class) {
            return (T) WfRun.fromProto(WFRunPb.parseFrom(b));
        } else if (cls == WfSpec.class) {
            return (T) WfSpec.fromProto(WFSpecPb.parseFrom(b));
        }
        return null;
    }
}

/*
 * Some random thoughts:
 * - each GETable has a partition key and an ID. They may be different.
 * - For example, we want TaskRun's for a WfRun to end up on the same host
 * - VariableValue's for a ThreadRun will end up on the same node as each other
 * - Will we query VariableValue's from the Scheduler topology or from the
 *   API topology?
 * 
 * Will we make it possible to deploy the Scheduler separately from the API?
 */