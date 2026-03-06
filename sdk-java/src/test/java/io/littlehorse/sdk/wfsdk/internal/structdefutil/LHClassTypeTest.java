package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.worker.adapter.LHTypeAdapterRegistry;
import org.junit.jupiter.api.Test;

public class LHClassTypeTest {
    @Test
    public void testGetCoreComponentType() {
        LHClassType lhClassType = LHClassType.fromJavaClass(String[][][][].class, LHTypeAdapterRegistry.empty());

        LHClassType expectedCoreComponentType = LHClassType.fromJavaClass(String.class, LHTypeAdapterRegistry.empty());
        LHClassType actualCoreComponentType = lhClassType.getCoreComponentType();

        assertThat(actualCoreComponentType).isEqualTo(expectedCoreComponentType);
    }
}
