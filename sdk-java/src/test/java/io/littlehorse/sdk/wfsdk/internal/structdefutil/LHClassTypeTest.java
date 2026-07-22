package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHStructDef;
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

    @LHStructDef("child-record")
    public record ChildRecord(String name) {}

    @LHStructDef("parent-class")
    public static class ParentClass {
        private ChildRecord child;

        public ChildRecord getChild() {
            return child;
        }

        public void setChild(ChildRecord child) {
            this.child = child;
        }
    }

    @LHStructDef("child-class")
    public static class ChildClass {
        private String name;

        public ChildClass() {}

        public ChildClass(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @LHStructDef("parent-record")
    public record ParentRecord(ChildClass child) {}

    @LHStructDef("record-with-default-ctor")
    public record RecordWithDefaultCtor(String greeting) {
        public RecordWithDefaultCtor() { this("hello"); }
    }

    @LHStructDef("config-no-default-ux")
    public record ConfigNoDefaultUx(String mode, int retries) {}

    @Test
    public void classWithRecordFieldRoundTrip() throws Exception {
        ParentClass p = new ParentClass();
        p.setChild(new ChildRecord("alice"));

        VariableValue v = LHLibUtil.objToVarVal(p);
        Object o = LHLibUtil.varValToObj(v, ParentClass.class);

        assertThat(o).isInstanceOf(ParentClass.class);
        ParentClass out = (ParentClass) o;
        assertThat(out.getChild()).isNotNull();
        assertThat(out.getChild().name()).isEqualTo("alice");
    }

    @Test
    public void recordWithClassFieldRoundTrip() throws Exception {
        ChildClass cc = new ChildClass("bob");
        ParentRecord pr = new ParentRecord(cc);

        VariableValue v = LHLibUtil.objToVarVal(pr);
        Object o = LHLibUtil.varValToObj(v, ParentRecord.class);

        assertThat(o).isInstanceOf(ParentRecord.class);
        ParentRecord out = (ParentRecord) o;
        assertThat(out.child()).isNotNull();
        assertThat(out.child().getName()).isEqualTo("bob");
    }

    @Test
    public void recordInstanceCreationPrefersNoArgOrFallsBackToCanonical() throws Exception {
        // record with no-arg ctor should yield greeting="hello"
        Object rec = LHClassType.fromJavaClass(RecordWithDefaultCtor.class, LHTypeAdapterRegistry.empty()).createInstance();
        assertThat(rec).isInstanceOf(RecordWithDefaultCtor.class);
        assertThat(((RecordWithDefaultCtor) rec).greeting()).isEqualTo("hello");

        // record without no-arg ctor should fall back to canonical with defaults
        Object cfg = LHClassType.fromJavaClass(ConfigNoDefaultUx.class, LHTypeAdapterRegistry.empty()).createInstance();
        assertThat(cfg).isInstanceOf(ConfigNoDefaultUx.class);
        assertThat(((ConfigNoDefaultUx) cfg).mode()).isNull();
        assertThat(((ConfigNoDefaultUx) cfg).retries()).isEqualTo(0);
    }
}
