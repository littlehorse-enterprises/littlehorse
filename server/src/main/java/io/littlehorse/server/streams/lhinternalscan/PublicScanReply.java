package io.littlehorse.server.streams.lhinternalscan;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * RP: The Response Protobuf
 * OP: The Individual Entry Protobuf
 * OJ: The Individual Entry Java Object
 */
public abstract class PublicScanReply<RP extends Message, OP extends Message, OJ extends LHSerializable<OP>>
        extends LHSerializable<RP> {

    public ByteString bookmark;
    public String message;
    public List<OJ> results;

    public PublicScanReply() {
        results = new ArrayList<>();
    }

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

            Method addResults = builderCls.getMethod("addResults", resCls);
            for (OJ result : results) {
                addResults.invoke(builder, result.toProto().build());
            }
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException exn) {
            throw new RuntimeException(exn);
        }
        return (GeneratedMessageV3.Builder<?>) builder;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) {
        Class<? extends GeneratedMessageV3> baseCls = getProtoBaseClass();

        try {
            Method hasBm = baseCls.getDeclaredMethod("hasBookmark");
            if ((boolean) hasBm.invoke(p)) {
                Method getBm = baseCls.getDeclaredMethod("getBookmark");
                bookmark = (ByteString) getBm.invoke(p);
            }

            Method getResults = baseCls.getDeclaredMethod("getResultsList");
            @SuppressWarnings("unchecked")
            List<OP> protoResults = (List<OP>) getResults.invoke(p);

            for (OP protoResult : protoResults) {
                results.add(LHSerializable.fromProto(protoResult, getResultJavaClass(), context));
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exn) {
            throw new RuntimeException(exn);
        }
    }

    public abstract Class<OP> getResultProtoClass();

    public abstract Class<OJ> getResultJavaClass();
}
