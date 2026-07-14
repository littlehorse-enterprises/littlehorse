package io.littlehorse.sdk.worker.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.ReturnType;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.TaskDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHType;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ScheduledTaskExecutorTest {

    private static class ReturnTasks {
        @LHType(isLHArray = true)
        public Long[] nativeArrayReturn() {
            return new Long[] {1L, 2L, 3L};
        }

        public Long[] jsonArrayReturn() {
            return new Long[] {1L, 2L, 3L};
        }

        public PlaceholderStruct placeholderStructReturn() {
            PlaceholderStruct out = new PlaceholderStruct();
            out.setName("Eve");
            return out;
        }

        public InlineStruct returnInlineStruct() {
            return InlineStruct.newBuilder()
                    .putFields(
                            "name",
                            StructField.newBuilder()
                                    .setValue(VariableValue.newBuilder().setStr("Han"))
                                    .build())
                    .build();
        }

        @LHType(structDefName = "${customerStructName}")
        public InlineStruct returnInlineStructWithPlaceholderAnnotation() {
            return InlineStruct.newBuilder()
                    .putFields(
                            "name",
                            StructField.newBuilder()
                                    .setValue(VariableValue.newBuilder().setStr("Leia"))
                                    .build())
                    .build();
        }
    }

    @LHStructDef("${company}-customer")
    public static class PlaceholderStruct {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void shouldSerializeReturnAsNativeArrayWhenAnnotated() throws Exception {
        ScheduledTaskExecutor executor = new ScheduledTaskExecutor(null, null, LHTypeAdapterRegistry.empty(), null);

        Method method = ReturnTasks.class.getMethod("nativeArrayReturn");
        VariableValue out = executor.serializeResult(new ReturnTasks().nativeArrayReturn(), method);

        assertThat(out.getValueCase()).isEqualTo(VariableValue.ValueCase.ARRAY);
        assertThat(out.getArray().getItemsCount()).isEqualTo(3);
        assertThat(out.getArray().getItems(0).getInt()).isEqualTo(1L);
    }

    @Test
    void shouldKeepJsonArraySerializationWhenNotAnnotated() throws Exception {
        ScheduledTaskExecutor executor = new ScheduledTaskExecutor(null, null, LHTypeAdapterRegistry.empty(), null);

        Method method = ReturnTasks.class.getMethod("jsonArrayReturn");
        VariableValue out = executor.serializeResult(new ReturnTasks().jsonArrayReturn(), method);

        assertThat(out.getValueCase()).isEqualTo(VariableValue.ValueCase.JSON_ARR);
    }

    @Test
    void shouldResolvePlaceholderInStructReturn() throws Exception {
        ScheduledTaskExecutor executor =
                new ScheduledTaskExecutor(null, null, LHTypeAdapterRegistry.empty(), null, Map.of("company", "acme"));

        Method method = ReturnTasks.class.getMethod("placeholderStructReturn");
        VariableValue out = executor.serializeResult(new ReturnTasks().placeholderStructReturn(), method);

        assertThat(out.getValueCase()).isEqualTo(VariableValue.ValueCase.STRUCT);
        assertThat(out.getStruct().getStructDefId().getName()).isEqualTo("acme-customer");
        assertThat(out.getStruct()
                        .getStruct()
                        .getFieldsMap()
                        .get("name")
                        .getValue()
                        .getStr())
                .isEqualTo("Eve");
    }

    @Test
    void shouldSerializeInlineStructReturnValueUsingTaskDefReturnType() throws Exception {
        TaskDef taskDef = taskDefWithStructReturn("acme-customer");
        ScheduledTaskExecutor executor =
                new ScheduledTaskExecutor(null, null, LHTypeAdapterRegistry.empty(), taskDef, Map.of());

        Method method = ReturnTasks.class.getMethod("returnInlineStruct");
        VariableValue out = executor.serializeResult(new ReturnTasks().returnInlineStruct(), method);

        assertThat(out.getValueCase()).isEqualTo(VariableValue.ValueCase.STRUCT);
        assertThat(out.getStruct().getStructDefId().getName()).isEqualTo("acme-customer");
        assertThat(out.getStruct()
                        .getStruct()
                        .getFieldsMap()
                        .get("name")
                        .getValue()
                        .getStr())
                .isEqualTo("Han");
    }

    @Test
    void shouldSerializeInlineStructReturnWithUnresolvedPlaceholderAnnotation() throws Exception {
        // Regression: the InlineStruct return path serializes using the server-registered TaskDef
        // return type, so it must not attempt to resolve the "${...}" placeholder on the return-type
        // annotation. Even with an empty placeholder map, serialization must succeed rather than throw.
        TaskDef taskDef = taskDefWithStructReturn("acme-customer");
        ScheduledTaskExecutor executor =
                new ScheduledTaskExecutor(null, null, LHTypeAdapterRegistry.empty(), taskDef, Map.of());

        Method method = ReturnTasks.class.getMethod("returnInlineStructWithPlaceholderAnnotation");
        VariableValue out =
                executor.serializeResult(new ReturnTasks().returnInlineStructWithPlaceholderAnnotation(), method);

        assertThat(out.getValueCase()).isEqualTo(VariableValue.ValueCase.STRUCT);
        assertThat(out.getStruct().getStructDefId().getName()).isEqualTo("acme-customer");
        assertThat(out.getStruct()
                        .getStruct()
                        .getFieldsMap()
                        .get("name")
                        .getValue()
                        .getStr())
                .isEqualTo("Leia");
    }

    private static TaskDef taskDefWithStructReturn(String structDefName) {
        return TaskDef.newBuilder()
                .setReturnType(ReturnType.newBuilder()
                        .setReturnType(TypeDefinition.newBuilder()
                                .setStructDefId(StructDefId.newBuilder().setName(structDefName))))
                .build();
    }
}
