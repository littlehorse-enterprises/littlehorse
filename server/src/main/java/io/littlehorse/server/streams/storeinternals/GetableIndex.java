package io.littlehorse.server.streams.storeinternals;

import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.proto.TagStorageType;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
public class GetableIndex<T extends AbstractGetable<?>> {

    private List<Pair<String, ValueType>> attributes;
    private Optional<TagStorageType> tagStorageType;
    private Predicate<T> predicate;

    public GetableIndex(
            List<Pair<String, ValueType>> attributes,
            Optional<TagStorageType> tagStorageType,
            Predicate<T> conditional) {
        this.attributes = attributes;
        this.tagStorageType = tagStorageType;
        this.predicate = conditional;
    }

    public GetableIndex(List<Pair<String, ValueType>> attributes, Optional<TagStorageType> tagStorageType) {
        this(attributes, tagStorageType, null);
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
