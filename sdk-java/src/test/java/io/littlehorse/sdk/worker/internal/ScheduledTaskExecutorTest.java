package io.littlehorse.sdk.worker.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHType;
import java.lang.reflect.Method;
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
    }

    @Test
    void shouldSerializeReturnAsNativeArrayWhenAnnotated() throws Exception {
        ScheduledTaskExecutor executor = new ScheduledTaskExecutor(null, null, LHTypeAdapterRegistry.empty(), null);
        Method serializeResult =
                ScheduledTaskExecutor.class.getDeclaredMethod("serializeResult", Object.class, Method.class);
        serializeResult.setAccessible(true);

        Method method = ReturnTasks.class.getMethod("nativeArrayReturn");
        VariableValue out =
                (VariableValue) serializeResult.invoke(executor, new ReturnTasks().nativeArrayReturn(), method);

        assertThat(out.getValueCase()).isEqualTo(VariableValue.ValueCase.ARRAY);
        assertThat(out.getArray().getItemsCount()).isEqualTo(3);
        assertThat(out.getArray().getItems(0).getInt()).isEqualTo(1L);
    }

    @Test
    void shouldKeepJsonArraySerializationWhenNotAnnotated() throws Exception {
        ScheduledTaskExecutor executor = new ScheduledTaskExecutor(null, null, LHTypeAdapterRegistry.empty(), null);
        Method serializeResult =
                ScheduledTaskExecutor.class.getDeclaredMethod("serializeResult", Object.class, Method.class);
        serializeResult.setAccessible(true);

        Method method = ReturnTasks.class.getMethod("jsonArrayReturn");
        VariableValue out =
                (VariableValue) serializeResult.invoke(executor, new ReturnTasks().jsonArrayReturn(), method);

        assertThat(out.getValueCase()).isEqualTo(VariableValue.ValueCase.JSON_ARR);
    }
}
