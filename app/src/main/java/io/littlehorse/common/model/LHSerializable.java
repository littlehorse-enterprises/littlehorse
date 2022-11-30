package io.littlehorse.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHSerdeError;
import java.lang.reflect.InvocationTargetException;

// `P` is the proto class used to serialize.
public abstract class LHSerializable<T extends MessageOrBuilder> {

    @JsonIgnore
    public abstract GeneratedMessageV3.Builder<?> toProto();

    public abstract void initFrom(MessageOrBuilder proto) throws LHSerdeError;

    @JsonIgnore
    public abstract Class<? extends GeneratedMessageV3> getProtoBaseClass();

    public String toJson() {
        try {
            return JsonFormat
                .printer()
                .includingDefaultValueFields()
                .print(toProto());
        } catch (InvalidProtocolBufferException exn) {
            throw new RuntimeException(exn);
        }
    }

    public byte[] toBytes(LHConfig config) {
        // TODO: figure out how we're going to get this to work with encryption.
        // Could just have a global static reference to an encryptor which we use
        // here and in the fromBytes() thing.
        return toProto().build().toByteArray();
    }

    // Probably don't want to use reflection for everything, but hey we gotta
    // get a prototype out the door.
    public static <T extends LHSerializable<?>> T fromBytes(
        byte[] b,
        Class<T> cls,
        LHConfig config
    ) throws LHSerdeError {
        try {
            T out = load(cls, config);
            Class<? extends GeneratedMessageV3> protoClass = out.getProtoBaseClass();

            GeneratedMessageV3 proto = protoClass.cast(
                protoClass.getMethod("parseFrom", byte[].class).invoke(null, b)
            );
            out.initFrom(proto);
            return out;
        } catch (Exception exn) {
            throw new LHSerdeError(
                exn,
                "unable to process bytes for " + cls.getName()
            );
        }
    }

    private static <T extends LHSerializable<?>> T load(
        Class<T> cls,
        LHConfig config
    )
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException exn) {
            return cls.getDeclaredConstructor(LHConfig.class).newInstance(config);
        }
    }

    public static <T extends LHSerializable<?>> T fromJson(
        String json,
        Class<T> cls,
        LHConfig config
    ) throws LHSerdeError {
        GeneratedMessageV3.Builder<?> builder;
        T out;

        try {
            out = load(cls, config);
            builder =
                (GeneratedMessageV3.Builder<?>) out
                    .getProtoBaseClass()
                    .getMethod("newBuilder")
                    .invoke(null);
        } catch (Exception exn) {
            throw new LHSerdeError(exn, "Failed to reflect the protobuilder");
        }

        try {
            JsonFormat.parser().merge(json, builder);
        } catch (InvalidProtocolBufferException exn) {
            throw new LHSerdeError(exn, "bad protobuf for " + cls.getName());
        }

        out.initFrom(builder.build());
        return out;
    }

    // public byte[] serialize() {
    //     return toProto().build().toByteArray();
    // }

    @Override
    public String toString() {
        return toJson();
    }
}
