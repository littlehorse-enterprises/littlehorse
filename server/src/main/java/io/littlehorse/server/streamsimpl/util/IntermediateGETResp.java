package io.littlehorse.server.streamsimpl.util;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntermediateGETResp<
        U extends Message, T extends LHSerializable<U>, V extends Message> {

    public String message;
    public LHResponseCode code;
    public T result;

    private Class<V> responseCls;

    public IntermediateGETResp(Class<V> responseCls) {
        this.responseCls = responseCls;
    }

    // EMPLOYEE_TODO: figure out why all my reflection is "unsafe" or "unchecked"
    @SuppressWarnings("unchecked")
    public V toProto() {
        try {
            GeneratedMessageV3.Builder<?> b =
                    (GeneratedMessageV3.Builder<?>)
                            responseCls.getMethod("newBuilder").invoke(null);
            if (message != null) {
                b.getClass().getMethod("setMessage", String.class).invoke(b, message);
            }
            b.getClass().getMethod("setCode", LHResponseCode.class).invoke(b, code);
            if (result != null) {
                U resultProto = (U) result.toProto().build();
                b.getClass().getMethod("setResult", resultProto.getClass()).invoke(b, resultProto);
            }
            return (V) b.build();
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new RuntimeException("Yikerz, not possible");
        }
    }
}
