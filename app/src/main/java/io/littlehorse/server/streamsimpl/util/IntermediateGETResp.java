package io.littlehorse.server.streamsimpl.util;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.LHResponseCodePb;

public class IntermediateGETResp<
    U extends MessageOrBuilder,
    T extends LHSerializable<U>,
    V extends MessageOrBuilder
> {

    public String message;
    public LHResponseCodePb code;
    public T result;

    private Class<V> responseCls;

    public IntermediateGETResp(Class<V> responseCls) {
        this.responseCls = responseCls;
    }

    // EMPLOYEE_TODO: figure out why all my reflection is "unsafe" or "unchecked"
    @SuppressWarnings("unchecked")
    public V toProto() {
        try {
            GeneratedMessageV3.Builder<?> b = (GeneratedMessageV3.Builder<?>) responseCls
                .getMethod("newBuilder")
                .invoke(null);
            if (message != null) {
                b.getClass().getMethod("setMessage", String.class).invoke(b, message);
            }
            b.getClass().getMethod("setCode", LHResponseCodePb.class).invoke(b, code);
            if (result != null) {
                U resultProto = (U) result.toProto().build();
                b
                    .getClass()
                    .getMethod("setResult", resultProto.getClass())
                    .invoke(b, resultProto);
            }
            return (V) b.build();
        } catch (Exception exn) {
            exn.printStackTrace();
            throw new RuntimeException("Yikerz, not possible");
        }
    }
}
