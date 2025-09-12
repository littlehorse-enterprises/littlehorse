package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LHClassTypeTest {
    @Test
    public void testGetCoreComponentType() {
        LHClassType lhClassType = LHClassType.fromJavaClass(String[][][][].class);

        LHClassType expectedCoreComponentType = LHClassType.fromJavaClass(String.class);
        LHClassType actualCoreComponentType = lhClassType.getCoreComponentType();

        assertThat(actualCoreComponentType).isEqualTo(expectedCoreComponentType);
    }
}
