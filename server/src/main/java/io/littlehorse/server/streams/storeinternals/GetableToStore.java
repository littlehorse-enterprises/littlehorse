package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.TagsCache;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetableToStore<U extends Message, T extends AbstractGetable<U>> {

    @Setter(AccessLevel.NONE)
    private final TagsCache tagsPresentBeforeUpdate;

    @Setter(AccessLevel.NONE)
    private final GetableClassEnum objectType;

    private T objectToStore;

    public GetableToStore(StoredGetable<U, T> thingInStore, Class<T> cls) {
        this.objectType = AbstractGetable.getTypeEnum(cls);

        if (thingInStore != null) {
            this.tagsPresentBeforeUpdate = thingInStore.getIndexCache();
            this.objectToStore = thingInStore.getStoredObject();
        } else {
            this.tagsPresentBeforeUpdate = new TagsCache(List.of());
        }
    }
}
