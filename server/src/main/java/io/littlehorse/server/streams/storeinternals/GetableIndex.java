package io.littlehorse.server.streams.storeinternals;

import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.proto.TagStorageType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
public class GetableIndex<T extends AbstractGetable<?>> {

    private final List<Pair<String, ValueType>> attributes;
    private final TagStorageType tagStorageType;
    private final Predicate<T> predicate;
    private final boolean counted;

    public GetableIndex(
            List<Pair<String, ValueType>> attributes,
            TagStorageType tagStorageType,
            Predicate<T> conditional,
            boolean counted) {
        this.attributes = Collections.unmodifiableList(attributes);
        this.tagStorageType = tagStorageType;
        this.predicate = conditional;
        this.counted = counted;
    }

    public GetableIndex(
            List<Pair<String, ValueType>> attributes, TagStorageType tagStorageType, Predicate<T> conditional) {
        this(attributes, tagStorageType, conditional, false);
    }

    public GetableIndex(List<Pair<String, ValueType>> attributes, TagStorageType tagStorageType) {
        this(attributes, tagStorageType, null);
    }

    // Mantained for code compatibility reasons
    // TODO: Remove this method
    @Deprecated
    public GetableIndex(List<Pair<String, ValueType>> attributes, Optional<TagStorageType> tagStorageType) {
        this(attributes, tagStorageType.get(), null);
    }

    public boolean searchAttributesMatch(List<String> searchAttributes) {
        return attributes.stream().map(Pair::getKey).toList().equals(searchAttributes);
    }

    @SuppressWarnings("unchecked")
    public <J extends AbstractGetable<?>> boolean isActiveOn(J getable) {
        if (this.predicate != null) {
            return predicate.test((T) getable);
        } else {
            return true;
        }
    }

    public enum ValueType {
        SINGLE,
        DYNAMIC,
    }
}
