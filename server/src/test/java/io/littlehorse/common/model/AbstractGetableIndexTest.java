package io.littlehorse.common.model;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public abstract class AbstractGetableIndexTest {

    @Test
    void shouldReturnTagsWithMatchingAttributeKeys() {
        AbstractGetable<?> getable = createGetable();
        List<String> expectedAttributes = getable.getIndexConfigurations().stream()
                .map(GetableIndex::getAttributes)
                .flatMap(pairs -> pairs.stream().map(Pair::getKey))
                .toList();
        List<String> actualAttributesFromTags = getable.getIndexConfigurations().stream()
                .map(GetableIndex::getAttributes)
                .flatMap(Collection::stream)
                .map(Pair::getKey)
                .toList();
        assertThat(expectedAttributes).containsAll(actualAttributesFromTags);
        ;
    }

    public abstract AbstractGetable<?> createGetable();

    public final Tag getTagForKeys(String... keys) {
        return getTagForKeys(createGetable(), keys);
    }

    public final Tag getTagForKeys(AbstractGetable<?> getable, String... keys) {
        for (Tag indexEntry : getable.getIndexEntries()) {
            boolean matches = indexEntry.getAttributes().stream()
                    .map(Attribute::getEscapedKey)
                    .toList()
                    .containsAll(List.of(keys));
            if (matches) {
                return indexEntry;
            }
        }
        return null;
    }

    @Test
    void shouldReturnOneTagPerIndexConfiguration() {
        AbstractGetable<?> getable = createGetable();
        assertThat(getable.getIndexEntries()).hasSameSizeAs(getable.getIndexConfigurations());
    }

    @Test
    void shouldSetDescribedObjectIdToGetableObjectId() {
        AbstractGetable<?> getable = createGetable();
        String expectedId = getable.getObjectId().toString();
        assertThat(getable.getIndexEntries())
                .extracting(tag -> tag.describedObjectId)
                .allMatch(expectedId::equals);
    }
}
