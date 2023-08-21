package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.proto.GetableClassEnum;
import java.lang.reflect.InvocationTargetException;

public abstract class ObjectIdModel<T extends Message, U extends Message, V extends AbstractGetable<U>>
        extends LHSerializable<T> {

    public abstract String getStoreKey();

    public abstract void initFrom(String storeKey);

    public abstract String getPartitionKey();

    public abstract GetableClassEnum getType();

    @SuppressWarnings("unchecked")
    public Class<V> getObjectClass() {
        return (Class<V>) AbstractGetable.getCls(getType());
    }

    @Override
    public String toString() {
        return getStoreKey();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!this.getClass().equals(other.getClass())) return false;

        return getStoreKey().equals(((ObjectIdModel<?, ?, ?>) other).getStoreKey());
    }

    @Override
    public int hashCode() {
        return getStoreKey().hashCode();
    }

    public static <T extends Message, U extends Message, V extends LHSerializable<U>> ObjectIdModel<?, ?, ?> fromString(
            String objectId, Class<? extends ObjectIdModel<?, ?, ?>> cls) {
        try {
            ObjectIdModel<?, ?, ?> id = cls.getDeclaredConstructor().newInstance();
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
