package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import org.junit.jupiter.api.Test;

public class LHClassTypeTest {
    @Test
    public void testGetCoreComponentType() {
        LHClassType lhClassType = LHClassType.fromJavaClass(String[][][][].class, LHTypeAdapterRegistry.empty());

        LHClassType expectedCoreComponentType = LHClassType.fromJavaClass(String.class, LHTypeAdapterRegistry.empty());
        LHClassType actualCoreComponentType = lhClassType.getCoreComponentType(LHTypeAdapterRegistry.empty());

        assertThat(actualCoreComponentType).isEqualTo(expectedCoreComponentType);
    }

    @Test
    public void shouldThrowExceptionForVoidClass() {
        assertThatThrownBy(() -> LHClassType.fromJavaClass(void.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Void type is not supported");
    }
}
