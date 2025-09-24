package io.littlehorse.server.streams.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.server.streams.store.StoredGetable;
import io.littlehorse.server.streams.storeinternals.index.TagsCache;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GetableToStore<U extends Message, T extends AbstractGetable<U>> {

    @Setter(AccessLevel.NONE)
    private final TagsCache tagsPresentBeforeUpdate;

    @Setter(AccessLevel.NONE)
    private final GetableClassEnum objectType;

    private final T objectToStore;

    private final U previouslyStoredProto;

    private final Class<T> cls;

    private final boolean containsUpdate;

    public boolean isDeletion() {
        return objectToStore == null;
    }

    public boolean containsUpdate() {
        return this.containsUpdate;
    }

    @SuppressWarnings("unchecked")
    public GetableToStore(T objectToStore, StoredGetable<U, T> thingInStore, Class<T> cls) {
        Objects.requireNonNull(objectToStore);
        this.cls = cls;
        this.objectType = AbstractGetable.getTypeEnum(cls);
        this.objectToStore = objectToStore;
        if (thingInStore != null) {
            this.tagsPresentBeforeUpdate = thingInStore.getIndexCache();
            this.previouslyStoredProto =
                    (U) (thingInStore.getStoredObject().toProto().build());
            this.containsUpdate = !objectToStore.toProto().build().equals(previouslyStoredProto);
        } else {
            this.tagsPresentBeforeUpdate = new TagsCache();
            this.previouslyStoredProto = null;
            this.containsUpdate = true;
        }
    }

    @SuppressWarnings("unchecked")
    public GetableToStore(T objectToStore, TagsCache indexCache, Class<T> cls) {
        Objects.requireNonNull(objectToStore);
        this.cls = cls;
        this.objectType = AbstractGetable.getTypeEnum(cls);
        this.objectToStore = objectToStore;
        this.tagsPresentBeforeUpdate = indexCache;
        this.previouslyStoredProto = null;
        this.containsUpdate = false;
    }

    private GetableToStore(Class<T> cls, TagsCache tagsPresent) {
        this.cls = cls;
        this.tagsPresentBeforeUpdate = tagsPresent;
        this.objectType = AbstractGetable.getTypeEnum(cls);
        this.objectToStore = null;
        this.previouslyStoredProto = null;
        this.containsUpdate = true;
    }

    public static <U extends Message, T extends AbstractGetable<U>> GetableToStore<U, T> deletion(
            Class<T> cls, TagsCache tagsToDelete) {
        return new GetableToStore<>(cls, tagsToDelete);
    }
}
