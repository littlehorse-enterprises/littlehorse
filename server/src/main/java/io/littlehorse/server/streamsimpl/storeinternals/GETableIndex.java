package io.littlehorse.server.streamsimpl.storeinternals;

import com.google.protobuf.Message;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.server.streamsimpl.storeinternals.index.Attribute;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;

public class GETableIndex {

    private Collection<Pair<String, Function<GETable<?>, List<String>>>> keys;
    private TagStorageTypePb tagStorageTypePb;
    private Predicate<GETable<?>> isGETableActive;

    private Class<? extends GETable<?>> target;

    public GETableIndex(
        Class<? extends GETable<?>> target,
        Collection<Pair<String, Function<GETable<?>, List<String>>>> keys,
        Predicate<GETable<?>> isGETableActive,
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
        return Tag.getAttributeString(GETable.getTypeEnum(target), attributes);
    }

    public Collection<String> getKeys() {
        return keys.stream().map(Pair::getLeft).toList();
    }

    private Function<GETable<?>, List<String>> findFunction(String key) {
        return keys
            .stream()
            .filter(stringFunctionPair -> stringFunctionPair.getLeft().equals(key))
            .map(Pair::getRight)
            .findFirst()
            .orElse(null);
    }

    public List<String> getValue(GETable<?> getable, String key) {
        return findFunction(key).apply(getable);
    }

    public boolean isActive(GETable<?> getable) {
        return isGETableActive.test(getable);
    }
}
