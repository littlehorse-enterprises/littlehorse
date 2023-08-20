package io.littlehorse.io.littlehorse.server.streamsimpl.storeinternals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.JsonIndexModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.sdk.common.proto.IndexType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.server.streamsimpl.coreprocessors.CommandProcessorOutput;
import io.littlehorse.server.streamsimpl.storeinternals.GetableStorageManager;
import io.littlehorse.server.streamsimpl.storeinternals.LHStoreWrapper;
import io.littlehorse.server.streamsimpl.storeinternals.index.Tag;
import io.littlehorse.server.streamsimpl.storeinternals.utils.LHIterKeyValue;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JsonVariableStorageManagerTest {

    private final KeyValueStore<String, Bytes> store = Stores.keyValueStoreBuilder(
            Stores.inMemoryKeyValueStore("myStore"), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    @Mock
    private LHConfig lhConfig;

    private LHStoreWrapper localStoreWrapper;

    final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();
    private GetableStorageManager geTableStorageManager;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() throws Exception {
        initializeDependencies();
        Map map = objectMapper.readValue(
                Paths.get("./src/test/resources/json-variables-example.json").toFile(), Map.class);
        VariableModel variable = TestUtil.variable("wfrun-id");
        variable.setName("testVariable");
        VariableDefModel variableDef = TestUtil.variableDef("testVariable", VariableType.JSON_OBJ);
        List<JsonIndexModel> indices = List.of(
                new JsonIndexModel("$.about", IndexType.LOCAL_INDEX),
                new JsonIndexModel("$.profile.email", IndexType.LOCAL_INDEX),
                new JsonIndexModel("$.tags", IndexType.LOCAL_INDEX),
                new JsonIndexModel("$.balance", IndexType.LOCAL_INDEX));
        variableDef.setJsonIndices(indices);
        variable.getWfSpecModel().getThreadSpecs().forEach((s, threadSpec) -> {
            threadSpec.setVariableDefs(List.of(variableDef));
        });
        VariableValueModel variableValue = new VariableValueModel();
        variableValue.setType(VariableType.JSON_OBJ);
        variableValue.setJsonObjVal(map);
        variable.setValue(variableValue);
        geTableStorageManager.store(variable);
    }

    private void initializeDependencies() {
        localStoreWrapper = new LHStoreWrapper(store, lhConfig);
        geTableStorageManager = new GetableStorageManager(localStoreWrapper, lhConfig, mockProcessorContext);
        store.init(mockProcessorContext.getStateStoreContext(), store);
    }

    private List<Tag> storedTags() {
        return localTagScan("").map(LHIterKeyValue::getValue).toList();
    }

    private Stream<LHIterKeyValue<Tag>> localTagScan(String keyPrefix) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        localStoreWrapper.prefixScan(keyPrefix, Tag.class), Spliterator.ORDERED),
                false);
    }

    private List<String> storedTagPrefixStoreKeys() {
        return storedTags().stream()
                .map(Tag::getStoreKey)
                .map(s -> s.split("/"))
                .map(strings -> strings[0] + "/" + strings[1])
                .toList();
    }

    @Test
    void storeLongAttributeValueText() {
        String expectedStoreKey = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.about_Consequat exercitation"
                + " officia ut mollit in aute amet. Consequat laborum elit id incididunt quis"
                + " aliquip pariatur magna eu velit ad dolore. Consectetur excepteur ut sit"
                + " magna magna sunt qui dolore est officia aliquip. Quis deserunt aliqua"
                + " consequat id et excepteur nulla qui. Id exercitation occaecat duis nostrud"
                + " quis cupidatat et nisi mollit non. Consectetur quis mollit magna Lorem anim"
                + " qui pariatur. Incididunt fugiat enim duis consequat mollit nisi elit"
                + " pariatur et excepteur id voluptate dolor.\r\n";
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains(expectedStoreKey);
    }

    @Test
    void storeEmailAttributeValue() {
        String expectedStoreKey = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.profile.email_forbesbooth@quarex.com";
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains(expectedStoreKey);
    }

    @Test
    void storeInnerArrayObject() {
        String expectedStoreKey = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.tags_[ex, fugiat, id,"
                + " labore, dolor, consectetur, veniam]";
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains(expectedStoreKey);
    }

    @Test
    void storeDoubleAttributeValue() {
        String expectedStoreKey = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.balance_2759.634399439295";
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains(expectedStoreKey);
    }

    @Test
    void preventStorageForNonIndexedAttributes() {
        String expectedStoreKey = "5/__wfSpecName_testWfSpecName__wfSpecVersion_00000__$.registered_2018-09-02T10:37:59"
                + " +05:00";
        Assertions.assertThat(storedTagPrefixStoreKeys()).doesNotContain(expectedStoreKey);
    }
}
