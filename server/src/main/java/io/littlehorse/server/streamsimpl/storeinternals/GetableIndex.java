package io.littlehorse.server.streamsimpl.storeinternals;

import io.littlehorse.common.model.Getable;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;

public class GetableIndex {

    private Collection<Pair<String, Function<Getable<?>, List<String>>>> keys;
    private TagStorageTypePb tagStorageTypePb;
    private Predicate<Getable<?>> isGETableActive;

    private Class<? extends Getable<?>> target;

    public GetableIndex(
        Class<? extends Getable<?>> target,
        Collection<Pair<String, Function<Getable<?>, List<String>>>> keys,
        Predicate<Getable<?>> isGETableActive,
        TagStorageTypePb tagStorageTypePb
    ) {
        this.tagStorageTypePb = tagStorageTypePb;
        this.keys = keys;
        this.isGETableActive = isGETableActive;
        this.target = target;
    }

    public TagStorageTypePb getTagStorageTypePb() {
        return tagStorageTypePb;
    }

    public String getPartitionKeyForAttrs(List<Attribute> attributes) {
        return Tag.getAttributeString(Getable.getTypeEnum(target), attributes);
    }

    public Collection<String> getKeys() {
        return keys.stream().map(Pair::getLeft).toList();
    }

    private Function<Getable<?>, List<String>> findFunction(String key) {
        return keys
            .stream()
            .filter(stringFunctionPair -> stringFunctionPair.getLeft().equals(key))
            .map(Pair::getRight)
            .findFirst()
            .orElse(null);
    }

    public List<String> getValue(Getable<?> getable, String key) {
        return findFunction(key).apply(getable);
    }

    public boolean isActive(Getable<?> getable) {
        return isGETableActive.test(getable);
    }
}
