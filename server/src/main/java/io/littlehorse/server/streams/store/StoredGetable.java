package io.littlehorse.server.streams.store;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.proto.StoredGetablePb;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.server.streams.storeinternals.index.TagsCache;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class StoredGetable<U extends Message, T extends AbstractGetable<U>> extends Storeable<StoredGetablePb> {

    private TagsCache indexCache;
    private T storedObject;
    private GetableClassEnum objectType;

    public StoredGetable() {}

    @SuppressWarnings("unchecked")
    public StoredGetable(T getable) {
        this.indexCache = new TagsCache(getable.getIndexEntries());
        this.storedObject = getable;
        this.objectType = AbstractGetable.getTypeEnum((Class<? extends AbstractGetable<?>>) getable.getClass());
    }

    @Override
    public Class<StoredGetablePb> getProtoBaseClass() {
        return StoredGetablePb.class;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        StoredGetablePb p = (StoredGetablePb) proto;
        indexCache = LHSerializable.fromProto(p.getIndexCache(), TagsCache.class, context);
        ByteString getablePayload = p.getGetablePayload();
        objectType = p.getType();
        try {
            storedObject = LHSerializable.fromBytes(getablePayload, getStoredClass(), context);
        } catch (LHSerdeException exception) {
            log.error("Failed loading from store: {}", exception.getMessage(), exception);
        }
    }

    @Override
    public StoredGetablePb.Builder toProto() {
        return StoredGetablePb.newBuilder()
                .setType(objectType)
                .setIndexCache(indexCache.toProto())
                .setGetablePayload(storedObject.toProto().build().toByteString());
    }

    @Override
    public String getStoreKey() {
        return StoredGetable.getStoreKey(storedObject.getObjectId());
    }

    public static String getLegacyStoreKey(ObjectIdModel<?, ?, ?> id) {
        // See the comment in `getStoreKey()` below. That's a new optimization. We have this code
        // here for a while in order to provide seamless backwards compatible migrations.
        return id.getType().getNumber() + "/" + id.toString();
    }

    public static String getStoreKey(ObjectIdModel<?, ?, ?> id) {
        // We want the stuff related to a single `WfRun` to be grouped under the same prefix so that
        // they end up on the same block (or on adjacent blocks) in rocksdb. This reduces the amount
        // of storage I/O round trips that we have to make when reading multiple cold Getable's from
        // the same WfRun.
        Optional<String> partitionKey = id.getPartitionKey();
        if (partitionKey.isPresent()) {
            String idWithoutPartitionKey =
                    id.toString().substring(partitionKey.get().length());
            return partitionKey.get() + "/" + id.getType().getNumber() + "/" + idWithoutPartitionKey;
        } else {
            return id.getType().getNumber() + "/" + id.toString();
        }
    }

    @SuppressWarnings("unchecked")
    public Class<T> getStoredClass() {
        return (Class<T>) AbstractGetable.getCls(objectType);
    }

    @Override
    public StoreableType getType() {
        return StoreableType.STORED_GETABLE;
    }

    public static String getRocksDBKey(String key, GetableClassEnum objType) {
        Class<? extends ObjectIdModel<?, ?, ?>> idCls = AbstractGetable.getIdCls(objType);
        ObjectIdModel<?, ?, ?> objectId = ObjectIdModel.fromString(key, idCls);
        return StoredGetable.getStoreKey(objectId);
    }
}
