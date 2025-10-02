package io.littlehorse.common.model.getable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.server.streams.store.StoredGetable;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public abstract class ObjectIdModel<T extends Message, U extends Message, V extends AbstractGetable<U>>
        extends LHSerializable<T> implements Comparable<ObjectIdModel<?, ?, ?>> {

    // Force the user to implement toString
    public abstract String toString();

    public abstract void initFromString(String storeKey);

    public abstract GetableClassEnum getType();

    public final String getStoreableKey() {
        return StoredGetable.getStoreKey(this);
    }

    public final String getLegacyStoreableKey() {
        return StoredGetable.getUngroupedStoreKey(getType(), this.toString());
    }

    // This can be overriden by CoreObjectId and RepartitionObjectId.
    // Note that MetadataId's will never have a partition key.
    public Optional<String> getPartitionKey() {
        return Optional.empty();
    }

    public abstract LHStore getStore();

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
        return toString().hashCode();
    }

    @Override
    public int compareTo(ObjectIdModel<?, ?, ?> other) {
        return toString().compareTo(other.toString());
    }

    public static <T extends Message, U extends Message, V extends LHSerializable<U>> ObjectIdModel<?, ?, ?> fromString(
            String key, Class<? extends ObjectIdModel<?, ?, ?>> cls) {
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
