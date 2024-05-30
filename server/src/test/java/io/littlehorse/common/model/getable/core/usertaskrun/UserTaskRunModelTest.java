package io.littlehorse.common.model.getable.core.usertaskrun;

import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserTaskRunModelTest {

    @Test
    void shouldHave15Indexes() {
        UserTaskRunModel utr = new UserTaskRunModel();
        Assertions.assertThat(utr.getIndexConfigurations()).size().isEqualTo(15);
    }

    @Test
    void indexesShouldBeOrdered() {
        UserTaskRunModel utr = new UserTaskRunModel();
        List<String> tagOrders = List.of("status", "userTaskDefName", "userId", "userGroup");

        Predicate<GetableIndex<? extends AbstractGetable<?>>> isProperlySorted = index -> {
            List<String> attributes = index.getAttributes().stream()
                    .map(attrib -> attrib.getLeft())
                    .toList();

            for (String attribute : attributes) {
                Assertions.assertThat(tagOrders).contains(attribute);
            }

            for (int i = 0; i < attributes.size() - 1; i++) {
                String first = attributes.get(i);
                String second = attributes.get(i + 1);

                if (tagOrders.indexOf(first) > tagOrders.indexOf(second)) {
                    return false;
                }
            }

            return true;
        };

        List<GetableIndex<? extends AbstractGetable<?>>> indices = utr.getIndexConfigurations();
        for (GetableIndex<? extends AbstractGetable<?>> index : indices) {
            Assertions.assertThat(isProperlySorted.test(index)).isTrue();
        }
    }

    @Test
    void indexesShouldBeUnique() {
        UserTaskRunModel utr = new UserTaskRunModel();
        Set<String> deduplicatedIndexes = utr.getIndexConfigurations().stream()
                .map(index -> {
                    return index.getAttributes().stream()
                            .map(attrib -> attrib.getLeft())
                            .toList();
                })
                .map(attribList -> {
                    return String.join("_", attribList);
                })
                .collect(Collectors.toSet());

        Assertions.assertThat(deduplicatedIndexes.size()).isEqualTo(15);
    }
}
