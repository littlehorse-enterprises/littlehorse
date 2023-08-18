package io.littlehorse.common.model;

import com.google.protobuf.Message;
import io.littlehorse.common.proto.GetableClassEnum;
import java.lang.reflect.InvocationTargetException;

public abstract class ObjectId<T extends Message, U extends Message, V extends Storeable<U>>
        extends LHSerializable<T> {

    public abstract String getStoreKey();

    public abstract void initFrom(String storeKey);

    public abstract String getPartitionKey();

    public abstract GetableClassEnum getType();

    @Override
    public String toString() {
        return getStoreKey();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!this.getClass().equals(other.getClass())) return false;

        return getStoreKey().equals(((ObjectId<?, ?, ?>) other).getStoreKey());
    }

    @Override
    public int hashCode() {
        return getStoreKey().hashCode();
    }

    public static <T extends Message, U extends Message, V extends LHSerializable<U>>
            ObjectId<?, ?, ?> fromString(String objectId, Class<? extends ObjectId<?, ?, ?>> cls) {
        try {
            ObjectId<?, ?, ?> id = cls.getDeclaredConstructor().newInstance();
            id.initFrom(objectId);
            return id;
        } catch (IllegalAccessException
                | InstantiationException
                | InvocationTargetException
                | NoSuchMethodException exn) {
            throw new RuntimeException(exn);
        }
    }
}
