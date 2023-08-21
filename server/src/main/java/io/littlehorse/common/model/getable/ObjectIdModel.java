package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.server.streams.store.StoredGetable;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public abstract class ObjectIdModel<T extends Message, U extends Message, V extends AbstractGetable<U>>
        extends LHSerializable<T> {

    // Force the user to implement toString
    public abstract String toString();

    public abstract void initFromString(String storeKey);

    public abstract GetableClassEnum getType();

    public final String getStoreableKey() {
        return StoredGetable.getStoreKey(this);
    }

    // This can be overriden by CoreObjectId and RepartitionObjectId.
    // Note that MetadataId's will never have a partition key.
    public Optional<String> getPartitionKey() {
        return Optional.empty();
    }

    public abstract LHStoreType getStore();

    @SuppressWarnings("unchecked")
    public Class<V> getObjectClass() {
        return (Class<V>) AbstractGetable.getCls(getType());
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!this.getClass().equals(other.getClass())) return false;

        return this.toString().equals(((ObjectIdModel<?, ?, ?>) other).toString());
    }

    @Override
    public int hashCode() {
        return getStoreableKey().hashCode();
    }

    @Deprecated(forRemoval = true)
    public static <T extends Message, U extends Message, V extends LHSerializable<U>>
            ObjectIdModel<?, ?, ?> fromStoreableKey(String key, Class<? extends ObjectIdModel<?, ?, ?>> cls) {
        try {
            ObjectIdModel<?, ?, ?> id = cls.getDeclaredConstructor().newInstance();
            id.initFromString(key);
            return id;
        } catch (IllegalAccessException
                | InstantiationException
                | InvocationTargetException
                | NoSuchMethodException exn) {
            throw new RuntimeException(exn);
        }
    }
}
