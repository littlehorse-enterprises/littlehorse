package io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.common.model.Getable;
import io.littlehorse.common.proto.TagStorageTypePb;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;

@Getter
public class GetableIndex<T extends Getable<?>> {

    private List<Pair<String, ValueType>> attributes;
    private Optional<TagStorageTypePb> tagStorageTypePb;
    private Predicate<T> conditional;

    public GetableIndex(
        List<Pair<String, ValueType>> attributes,
        Optional<TagStorageTypePb> tagStorageTypePb,
        Predicate<T> conditional
    ) {
        this.attributes = attributes;
        this.tagStorageTypePb = tagStorageTypePb;
        this.conditional = conditional;
    }

    public GetableIndex(
        List<Pair<String, ValueType>> attributes,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        this(attributes, tagStorageTypePb, null);
    }

    public boolean searchAttributesMatch(List<String> searchAttributes) {
        return attributes
            .stream()
            .map(Pair::getKey)
            .toList()
            .equals(searchAttributes);
    }

    @SuppressWarnings("unchecked")
    public <J extends Getable<?>> boolean isValid(J getable) {
        if (conditional != null) {
            return conditional.test((T) getable);
        } else {
            return true;
        }
    }

    public enum ValueType {
        SINGLE,
        DYNAMIC,
    }
}
