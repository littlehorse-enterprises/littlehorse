package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.*;
import io.littlehorse.sdk.worker.*;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

class LHStructOneOfTest {
    @LHStructDef("payment")
    public record Fields(
            String orderId,
            @LHStructOneOf(group = "method") String card,
            @LHStructOneOf(group = "method") String bank) {}

    @LHStructOneOf
    public record Method(String card, String bank) {}

    @LHStructDef("payment")
    public record Helper(String orderId, Method method) {}

    @LHStructDef("collision")
    public record Collision(String card, Method method) {}

    @LHStructDef("primitive")
    public record Primitive(@LHStructOneOf(group = "method") int number) {}

    @LHStructDef("nullable")
    public record Nullable(@LHStructOneOf(group = "method") @LHStructField(isNullable = true) String card) {}

    @LHStructDef("collections")
    public record Collections(
            @LHStructOneOf(group = "choice") String[] array,
            @LHStructOneOf(group = "choice") Map<String, Integer> map,
            @LHStructOneOf(group = "choice") Boolean flag) {}

    @Getter
    @Setter
    public static class Base {
        @LHStructOneOf(group = "method")
        private String card;
    }

    @Getter
    @Setter
    @LHStructDef("inherited")
    public static class Child extends Base {
        @LHStructOneOf(group = "method")
        private String bank;
    }

    @Getter
    @Setter
    @LHStructDef("defaults")
    public static class Defaults {
        @LHStructOneOf(group = "method")
        private String card = "default";
    }

    @Getter
    @Setter
    @LHStructOneOf
    public static class PojoMethod {
        private String card;
        private String bank;
    }

    @Getter
    @Setter
    @LHStructDef("pojo-helper")
    public static class PojoPayment {
        private PojoMethod method;
    }

    @LHStructDef("mixed")
    public record Mixed(
            Method method,
            @LHStructOneOf(group = "other") Integer number,
            @LHStructOneOf(group = "other") String text,
            Helper child) {}

    @LHStructDef("repeat")
    public record Repeated(Method method, Method backup) {}

    @LHStructOneOf
    @LHStructDef("invalid-helper")
    public record NamedHelper(String card) {}

    @LHStructOneOf
    public record MixedHelper(@LHStructOneOf(group = "bad") String card) {}

    @LHStructDef("bad-parent")
    public record BadParent(MixedHelper method) {}

    @LHStructDef("conflicting")
    public static class Conflicting {
        @LHStructOneOf(group = "first")
        public String getCard() {
            return null;
        }

        @LHStructOneOf(group = "second")
        public void setCard(String value) {}
    }

    @Test
    void helpersSupportPojosAndIndependentParentChildGroups() {
        PojoPayment payment = new PojoPayment();
        PojoMethod method = new PojoMethod();
        method.setBank("bank");
        payment.setMethod(method);
        var restored = (PojoPayment) LHLibUtil.varValToObj(LHLibUtil.objToVarVal(payment), PojoPayment.class);
        assertThat(restored.getMethod().getBank()).isEqualTo("bank");
        assertThat(restored.getMethod().getCard()).isNull();
        Mixed mixed = new Mixed(new Method("card", null), 0, null, new Helper("child", new Method(null, "bank")));
        assertThat(LHLibUtil.varValToObj(LHLibUtil.objToVarVal(mixed), Mixed.class))
                .isEqualTo(mixed);
        for (var type : new Class<?>[] {Repeated.class, NamedHelper.class, BadParent.class, Conflicting.class}) {
            assertThatThrownBy(() -> new LHStructDefType(type)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void helperAndFieldDeclarationsHaveIdenticalSchemasAndWireValues() {
        assertThat(new LHStructDefType(Fields.class).getInlineStructDef())
                .isEqualTo(new LHStructDefType(Helper.class).getInlineStructDef());
        var fields = LHLibUtil.objToVarVal(new Fields("order", "token", null));
        var helper = LHLibUtil.objToVarVal(new Helper("order", new Method("token", null)));
        assertThat(helper).isEqualTo(fields);
        assertThat(helper.getStruct().getStruct().getFieldsMap()).containsOnlyKeys("orderId", "card");
        assertThat(LHLibUtil.varValToObj(helper, Helper.class))
                .isEqualTo(new Helper("order", new Method("token", null)));
        assertThat(LHLibUtil.varValToObj(fields, Fields.class)).isEqualTo(new Fields("order", "token", null));
    }

    @Test
    void nullHelpersReadAsEmptyHelpers() {
        var value = LHLibUtil.objToVarVal(new Helper("order", null));
        assertThat(value).isEqualTo(LHLibUtil.objToVarVal(new Helper("order", new Method(null, null))));
        assertThat(LHLibUtil.varValToObj(value, Helper.class)).isEqualTo(new Helper("order", new Method(null, null)));
    }

    @Test
    void rejectsConflictsOnWriteAndRead() {
        assertThatThrownBy(() -> LHLibUtil.objToVarVal(new Fields("order", "card", "bank")))
                .hasMessageContaining("selects both");
        var value = LHLibUtil.objToVarVal(new Fields("order", "card", null));
        var bad = value.toBuilder()
                .setStruct(value.getStruct().toBuilder()
                        .setStruct(value.getStruct().getStruct().toBuilder()
                                .putFields(
                                        "bank",
                                        StructField.newBuilder()
                                                .setValue(VariableValue.newBuilder()
                                                        .setStr("bank"))
                                                .build())))
                .build();
        assertThatThrownBy(() -> LHLibUtil.varValToObj(bad, Fields.class)).hasMessageContaining("selects both");
        var explicitNull = value.toBuilder()
                .setStruct(value.getStruct().toBuilder()
                        .setStruct(value.getStruct().getStruct().toBuilder()
                                .putFields("bank", StructField.getDefaultInstance())))
                .build();
        assertThatThrownBy(() -> LHLibUtil.varValToObj(explicitNull, Helper.class))
                .hasMessageContaining("explicit null");
    }

    @Test
    void rejectsInvalidDeclarationsAndFlatteningCollisions() {
        for (var type : new Class<?>[] {Collision.class, Primitive.class, Nullable.class, Defaults.class}) {
            assertThatThrownBy(() -> new LHStructDefType(type)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void countsEmptyCollectionsAndFalseAsSelections() {
        var value = LHLibUtil.objToVarVal(new Collections(null, Map.of(), null));
        assertThat(LHLibUtil.varValToObj(value, Collections.class)).isEqualTo(new Collections(null, Map.of(), null));
        assertThatThrownBy(() -> LHLibUtil.objToVarVal(new Collections(new String[0], Map.of(), null)))
                .hasMessageContaining("selects both");
        assertThat(LHLibUtil.objToVarVal(new Collections(null, null, false))
                        .getStruct()
                        .getStruct()
                        .containsFields("flag"))
                .isTrue();
    }

    @Test
    void inheritedPropertiesShareGroupsAndUnsetFieldsRestoreNull() {
        Child child = new Child();
        child.setCard("card");
        var value = LHLibUtil.objToVarVal(child);
        Child restored = (Child) LHLibUtil.varValToObj(value, Child.class);
        assertThat(restored.getCard()).isEqualTo("card");
        assertThat(restored.getBank()).isNull();
        child.setBank("bank");
        assertThatThrownBy(() -> LHLibUtil.objToVarVal(child)).hasMessageContaining("selects both");
    }
}
