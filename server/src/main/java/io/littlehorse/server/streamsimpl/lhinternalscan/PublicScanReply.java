package io.littlehorse.server.streamsimpl.lhinternalscan;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * RP: The Response Protobuf
 * OP: The Individual Entry Protobuf
 * OJ: The Individual Entry Java Object
 */
public abstract class PublicScanReply<
    RP extends Message, OP extends Message, OJ extends LHSerializable<OP>
>
    extends LHSerializable<RP> {

    public ByteString bookmark;
    public LHResponseCodePb code;
    public String message;
    public List<OJ> results;

    public PublicScanReply() {
        results = new ArrayList<>();
    }

    // EMPLOYEE_TODO: Optimize this a bit since reflection is "slow".
    public GeneratedMessageV3.Builder<?> toProto() {
        Class<? extends GeneratedMessageV3> base = getProtoBaseClass();
        Class<OP> resCls = getResultProtoClass();

        RP.Builder builder;
        try {
            builder = (Message.Builder) base.getMethod("newBuilder").invoke(null);
            Class<?> builderCls = builder.getClass();

            if (bookmark != null) {
                Method setBm = builderCls.getMethod("setBookmark", ByteString.class);
                setBm.invoke(builder, bookmark);
            }

            if (message != null) {
                Method setMsg = builderCls.getMethod("setMessage", String.class);
                setMsg.invoke(builder, message);
            }

            Method setCode = builderCls.getMethod("setCode", LHResponseCodePb.class);
            setCode.invoke(builder, code);

            Method addResults = builderCls.getMethod("addResults", resCls);
            for (OJ result : results) {
                addResults.invoke(builder, result.toProto().build());
            }
        } catch (
            InvocationTargetException
            | NoSuchMethodException
            | IllegalAccessException exn
        ) {
            throw new RuntimeException(exn);
        }
        return (GeneratedMessageV3.Builder<?>) builder;
    }

    public void initFrom(Message p) {
        Class<? extends GeneratedMessageV3> baseCls = getProtoBaseClass();

        try {
            Method hasBm = baseCls.getDeclaredMethod("hasBookmark");
            if ((boolean) hasBm.invoke(p)) {
                Method getBm = baseCls.getDeclaredMethod("getBookmark");
                bookmark = (ByteString) getBm.invoke(p);
            }

            Method hasMsg = baseCls.getDeclaredMethod("hasMessage");
            if ((boolean) hasMsg.invoke(p)) {
                Method getMsg = baseCls.getDeclaredMethod("getMessage");
                message = (String) getMsg.invoke(p);
            }

            Method getCode = baseCls.getDeclaredMethod("getCode");
            code = (LHResponseCodePb) getCode.invoke(p);

            Method getResults = baseCls.getDeclaredMethod("getResultsList");
            @SuppressWarnings("unchecked")
            List<OP> protoResults = (List<OP>) getResults.invoke(p);

            for (OP protoResult : protoResults) {
                results.add(
                    LHSerializable.fromProto(protoResult, getResultJavaClass())
                );
            }
        } catch (
            NoSuchMethodException
            | InvocationTargetException
            | IllegalAccessException exn
        ) {
            throw new RuntimeException(exn);
        }
    }

    public abstract Class<OP> getResultProtoClass();

    public abstract Class<OJ> getResultJavaClass();
}
