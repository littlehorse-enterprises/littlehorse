package io.littlehorse.storeinternals;

import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.TestUtil;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadVarDefModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.JsonIndexModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private LHServerConfig lhConfig;

    private TenantScopedStore storeWrapper;

    private String tenantId = LHConstants.DEFAULT_TENANT;

    final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();
    private GetableManager getableManager;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() throws Exception {
        initializeDependencies();

        // JSON_OBJ test
        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.readValue(
                Paths.get("./src/test/resources/json-variables-example.json").toFile(), Map.class);

        VariableModel jsonObjVariable = TestUtil.variable("wfrun-id");
        jsonObjVariable.getId().setName("testVariable");
        VariableDefModel variableDef = TestUtil.variableDef("testVariable", VariableType.JSON_OBJ);
        List<JsonIndexModel> indices = List.of(
                new JsonIndexModel("$.about", VariableType.STR),
                new JsonIndexModel("$.profile.email", VariableType.STR),
                new JsonIndexModel("$.tags", VariableType.JSON_ARR),
                new JsonIndexModel("$.balance", VariableType.STR));
        jsonObjVariable.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            threadSpec.setVariableDefs(
                    List.of(new ThreadVarDefModel(variableDef, indices, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });
        VariableValueModel jsonObjVal = new VariableValueModel(map);
        jsonObjVariable.setValue(jsonObjVal);
        getableManager.put(jsonObjVariable);

        // JSON_ARR test
        VariableDefModel jsonArrVarDef = TestUtil.variableDef("test", VariableType.JSON_ARR);
        VariableValueModel varVal = VariableValueModel.fromProto(
                LHLibUtil.objToVarVal(List.of(
                        "asdf",
                        "fdsa",
                        "asdf",
                        1234,
                        Map.of("foo", "bar"),
                        List.of("this-in-a-sublist", Map.of("sublistkey", "sublistval")))),
                null);

        VariableModel jsonArrVar = TestUtil.variable("test");
        jsonArrVar.getWfSpec().getThreadSpecs().forEach((s, threadSpec) -> {
            threadSpec.setVariableDefs(
                    List.of(new ThreadVarDefModel(jsonArrVarDef, true, false, WfRunVariableAccessLevel.PRIVATE_VAR)));
        });
        jsonArrVar.setValue(varVal);
        getableManager.put(jsonArrVar);
        getableManager.commit();
    }

    private void initializeDependencies() {
        storeWrapper = TenantScopedStore.newInstance(store, new TenantIdModel(tenantId), mock());
        getableManager = new GetableManager(storeWrapper, mockProcessorContext, lhConfig, mock(), mock());
        store.init(mockProcessorContext.getStateStoreContext(), store);
    }

    private List<Tag> storedTags() {
        return localTagScan("5/").map(LHIterKeyValue::getValue).toList();
    }

    private Stream<LHIterKeyValue<Tag>> localTagScan(String keyPrefix) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(storeWrapper.prefixScan(keyPrefix, Tag.class), Spliterator.ORDERED),
                false);
    }

    private List<String> storedTagPrefixStoreKeys() {
        return storedTags().stream()
                .map(Tag::getStoreKey)
                .map(storeKey -> {
                    String[] split = storeKey.split("/");
                    List<String> out = new ArrayList<>();
                    for (int i = 0; i < split.length - 4; i++) {
                        out.add(split[i]);
                    }
                    return String.join("/", out);
                })
                .toList();
    }

    @Test
    void storeLongAttributeValueText() {
        String expectedStoreKey = "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.about_"
                + LHUtil.toLHDbSearchFormat("Consequat exercitation"
                        + " officia ut mollit in aute amet. Consequat laborum elit id incididunt quis"
                        + " aliquip pariatur magna eu velit ad dolore. Consectetur excepteur ut sit"
                        + " magna magna sunt qui dolore est officia aliquip. Quis deserunt aliqua"
                        + " consequat id et excepteur nulla qui. Id exercitation occaecat duis nostrud"
                        + " quis cupidatat et nisi mollit non. Consectetur quis mollit magna Lorem anim"
                        + " qui pariatur. Incididunt fugiat enim duis consequat mollit nisi elit"
                        + " pariatur et excepteur id voluptate dolor.\r\n");

        String prefixToIgnore = "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.about_";
        int ignoredLength = prefixToIgnore.length();

        Assertions.assertThat(expectedStoreKey.length()).isEqualTo(64 + 16 + ignoredLength);
        Assertions.assertThat(expectedStoreKey.substring(ignoredLength)).startsWith("Consequat exercitation");

        Assertions.assertThat(storedTagPrefixStoreKeys()).contains(expectedStoreKey);
    }

    @Test
    void storeEmailAttributeValue() {
        String expectedStoreKey =
                "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.profile.email_forbesbooth@quarex.com";
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains(expectedStoreKey);
    }

    @Test
    void storeInnerArrayObject() {
        List<String> expectedKeys = List.of(
                "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.tags_ex",
                "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.tags_fugiat",
                "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.tags_id",
                "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.tags_labore",
                "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.tags_dolor",
                "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.tags_consectetur",
                "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.tags_veniam");
        Assertions.assertThat(storedTagPrefixStoreKeys()).containsAll(expectedKeys);
    }

    @Test
    void storeDoubleAttributeValue() {
        String expectedStoreKey = "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.balance_2759.634399439295";
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains(expectedStoreKey);
    }

    @Test
    void preventStorageForNonIndexedAttributes() {
        String expectedStoreKey =
                "5/__wfSpecId_testWfSpecName/00000/00000__testVariable_$.registered_2018-09-02T10:37:59" + " +05:00";
        Assertions.assertThat(storedTagPrefixStoreKeys()).doesNotContain(expectedStoreKey);
    }

    @Test
    void storeJsonArrIndexes() {
        List<String> expectedKeys = List.of(
                "5/__wfSpecId_testWfSpecName/00000/00000__test_asdf",
                "5/__wfSpecId_testWfSpecName/00000/00000__test_fdsa",
                "5/__wfSpecId_testWfSpecName/00000/00000__test_1234",
                "5/__wfSpecId_testWfSpecName/00000/00000__test_$.foo_bar");
        Assertions.assertThat(storedTagPrefixStoreKeys()).containsAll(expectedKeys);
    }
}
