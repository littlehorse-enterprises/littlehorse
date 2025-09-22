package io.littlehorse.common;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.lang.reflect.InvocationTargetException;
import lombok.extern.slf4j.Slf4j;

// `P` is the proto class used to serialize.
@Slf4j
public abstract class LHSerializable<T extends Message> {

    public abstract GeneratedMessage.Builder<?> toProto();

    public abstract void initFrom(Message proto, ExecutionContext context) throws LHSerdeException;

    // TODO: should this be:
    // public abstract Class<T> getProtoBaseClass(); ?
    public abstract Class<? extends GeneratedMessage> getProtoBaseClass();

    public String toJson() {
        try {
            return JsonFormat.printer().includingDefaultValueFields().print(toProto());
        } catch (InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }

    public byte[] toBytes() {
        return toProto().build().toByteArray();
    }

    public static <U extends Message, T extends LHSerializable<U>> T fromProto(
            Message proto, Class<T> cls, ExecutionContext context) {
        try {
            T out = cls.getDeclaredConstructor().newInstance();
            out.initFrom(proto, context);
            return out;
        } catch (NoSuchMethodException
                | InvocationTargetException
                | InstantiationException
                | IllegalAccessException exn) {
            log.error("This shouldn't be possible", exn);
            throw new RuntimeException(exn);
        }
    }

    // Probably don't want to use reflection for everything, but hey we gotta
    // get a prototype out the door.
    public static <T extends LHSerializable<?>> T fromBytes(byte[] b, Class<T> cls, ExecutionContext context)
            throws LHSerdeException {
        try {
            T out = load(cls);
            Class<? extends GeneratedMessage> protoClass = out.getProtoBaseClass();

            GeneratedMessage proto = protoClass.cast(
                    protoClass.getMethod("parseFrom", byte[].class).invoke(null, b));
            out.initFrom(proto, context);
            return out;
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new LHSerdeException(exn, "unable to process bytes for " + cls.getName());
        }
    }

    public static <T extends LHSerializable<?>> T fromBytes(ByteString b, Class<T> cls, ExecutionContext context)
            throws LHSerdeException {
        try {
            T out = load(cls);
            Class<? extends GeneratedMessage> protoClass = out.getProtoBaseClass();

            GeneratedMessage proto = protoClass.cast(
                    protoClass.getMethod("parseFrom", ByteString.class).invoke(null, b));
            out.initFrom(proto, context);
            return out;
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new LHSerdeException(exn, "unable to process bytes for " + cls.getName());
        }
    }

    public static <T extends LHSerializable<?>> GeneratedMessage protoFromBytes(byte[] b, Class<T> cls) {
        try {
            T out = load(cls);
            Class<? extends GeneratedMessage> protoClass = out.getProtoBaseClass();

            GeneratedMessage proto = protoClass.cast(
                    protoClass.getMethod("parseFrom", byte[].class).invoke(null, b));
            return proto;
        } catch (Exception exn) {
            log.error(exn.getMessage(), exn);
            throw new LHSerdeException(exn, "unable to process bytes for " + cls.getName());
        }
    }

    private static <T extends LHSerializable<?>> T load(Class<T> cls)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        return cls.getDeclaredConstructor().newInstance();
    }

    public static <T extends LHSerializable<?>> T fromJson(String json, Class<T> cls, ExecutionContext context)
            throws LHSerdeException {
        GeneratedMessage.Builder<?> builder;
        T out;

        try {
            out = load(cls);
            builder = (GeneratedMessage.Builder<?>)
                    out.getProtoBaseClass().getMethod("newBuilder").invoke(null);
        } catch (Exception exn) {
            throw new LHSerdeException(exn, "Failed to reflect the protobuilder");
        }

        try {
            JsonFormat.parser().merge(json, builder);
        } catch (InvalidProtocolBufferException exn) {
            throw new LHSerdeException(exn, "bad protobuf for " + cls.getName());
        }

        out.initFrom(builder.build(), context);
        return out;
    }

    // public byte[] serialize() {
    // return toProto().build().toByteArray();
    // }

    @Override
    public String toString() {
        return toJson();
    }
}
