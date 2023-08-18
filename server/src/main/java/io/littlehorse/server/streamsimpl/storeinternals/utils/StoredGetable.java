package io.littlehorse.server.streamsimpl.storeinternals.utils;

import com.google.protobuf.Message;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.Storeable;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.StoredGetablePb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streamsimpl.storeinternals.index.TagsCache;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class StoredGetable<U extends Message, T extends Getable<U>>
        extends Storeable<StoredGetablePb> {

    private TagsCache indexCache;
    private T storedObject;
    private GetableClassEnum objectType;

    public StoredGetable(TagsCache indexCache, T storedObject, GetableClassEnum objectType) {
        this.indexCache = indexCache;
        this.storedObject = storedObject;
        this.objectType = objectType;
    }

    public StoredGetable() {}

    @Override
    public Class<StoredGetablePb> getProtoBaseClass() {
        return StoredGetablePb.class;
    }

    @Override
    public void initFrom(Message proto) {
        StoredGetablePb p = (StoredGetablePb) proto;
        indexCache = LHSerializable.fromProto(p.getIndexCache(), TagsCache.class);
        objectType = p.getType();
        try {
            storedObject =
                    LHSerializable.fromBytes(
                            p.getGetablePayload().toByteArray(), getStoredClass(), null);
        } catch (LHSerdeError exception) {
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

    public static String getStoreKey(ObjectId<?, ?, ?> id) {
        return id.getType().getNumber() + "/" + id.getStoreKey();
    }

    @SuppressWarnings("unchecked")
    public Class<T> getStoredClass() {
        return (Class<T>) Getable.getCls(objectType);
    }
}
