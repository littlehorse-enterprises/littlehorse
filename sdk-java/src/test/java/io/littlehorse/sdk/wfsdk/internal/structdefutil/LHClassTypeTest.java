package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class LHClassTypeTest {
    public static class UnannotatedPojo {
        public String getName() {
            return "name";
        }
    }

    @Test
    public void testGetCoreComponentType() {
        LHClassType lhClassType = LHClassType.fromJavaClass(String[][][][].class, LHTypeAdapterRegistry.empty());

        LHClassType expectedCoreComponentType = LHClassType.fromJavaClass(String.class, LHTypeAdapterRegistry.empty());
        LHClassType actualCoreComponentType = lhClassType.getCoreComponentType(LHTypeAdapterRegistry.empty());

        assertThat(actualCoreComponentType).isEqualTo(expectedCoreComponentType);
    }

    @Test
    public void shouldThrowExceptionForVoidPrimitiveClass() {
        assertThatThrownBy(() -> LHClassType.fromJavaClass(void.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Void type is not supported");
    }

    @Test
    public void shouldThrowExceptionForVoidWrapperClass() {
        assertThatThrownBy(() -> LHClassType.fromJavaClass(Void.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Void type is not supported");
    }

    @Test
    public void shouldResolveUnannotatedPojoAccordingToContext() {
        LHClassType valueType = LHClassType.resolve(
                UnannotatedPojo.class, LHTypeAdapterRegistry.empty(), Map.of(), LHClassType.ResolutionContext.VALUE);
        LHClassType structMemberType = LHClassType.resolve(
                UnannotatedPojo.class,
                LHTypeAdapterRegistry.empty(),
                Map.of(),
                LHClassType.ResolutionContext.STRUCT_MEMBER);

        assertThat(valueType.getDefinedTypeCase()).isEqualTo(DefinedTypeCase.PRIMITIVE_TYPE);
        assertThat(structMemberType.getDefinedTypeCase()).isEqualTo(DefinedTypeCase.INLINE_STRUCT_DEF);
    }

    @Test
    public void shouldPropagateResolutionContextToArrayElementsAndMapValues() {
        LHArrayType arrayType = new LHArrayType(
                UnannotatedPojo[].class,
                LHTypeAdapterRegistry.empty(),
                Map.of(),
                LHClassType.ResolutionContext.STRUCT_MEMBER);
        LHMapType mapType = new LHMapType(
                String.class,
                UnannotatedPojo.class,
                LHTypeAdapterRegistry.empty(),
                Map.of(),
                LHClassType.ResolutionContext.STRUCT_MEMBER);

        assertThat(arrayType.getResolvedComponentType().getDefinedTypeCase())
                .isEqualTo(DefinedTypeCase.INLINE_STRUCT_DEF);
        assertThat(mapType.getResolvedValueType().getDefinedTypeCase()).isEqualTo(DefinedTypeCase.INLINE_STRUCT_DEF);
        assertThatThrownBy(() -> new LHArrayType(
                        UnannotatedPojo[].class,
                        LHTypeAdapterRegistry.empty(),
                        Map.of(),
                        LHClassType.ResolutionContext.VALUE))
                .hasMessageContaining("JSON_OBJ");
        assertThatThrownBy(() -> new LHMapType(
                        String.class,
                        UnannotatedPojo.class,
                        LHTypeAdapterRegistry.empty(),
                        Map.of(),
                        LHClassType.ResolutionContext.VALUE))
                .hasMessageContaining("JSON_OBJ");
    }
}
