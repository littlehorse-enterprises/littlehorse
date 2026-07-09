package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.sdk.worker.LHStructDef;
import java.util.Map;
import lombok.Getter;
import org.junit.jupiter.api.Test;

public class LHStructDefPlaceholderTest {

    private static final Map<String, String> PLACEHOLDERS = Map.of("company", "acme");

    @LHStructDef("${company}-address")
    @Getter
    static class Address {
        public String street;
        public String city;
    }

    @LHStructDef("${company}-person")
    @Getter
    static class Person {
        public String name;
        public Address homeAddress;
    }

    @LHStructDef("${company}-library")
    @Getter
    static class Library {
        public String name;
        public Address[] addresses;
    }

    @LHStructDef("plain-address")
    @Getter
    static class PlainAddress {
        public String street;
    }

    @Test
    public void shouldResolvePlaceholderInStructDefId() {
        LHStructDefType type = new LHStructDefType(Address.class, LHTypeAdapterRegistry.empty(), PLACEHOLDERS);

        assertThat(type.getStructDefId().getName()).isEqualTo("acme-address");
    }

    @Test
    public void shouldResolvePlaceholderInTypeDefinition() {
        LHStructDefType type = new LHStructDefType(Address.class, LHTypeAdapterRegistry.empty(), PLACEHOLDERS);

        assertThat(type.getTypeDefinition().getStructDefId().getName()).isEqualTo("acme-address");
    }

    @Test
    public void shouldResolvePlaceholderInPutStructDefRequest() {
        LHStructDefType type = new LHStructDefType(Address.class, LHTypeAdapterRegistry.empty(), PLACEHOLDERS);

        PutStructDefRequest request = type.toPutStructDefRequest();

        assertThat(request.getName()).isEqualTo("acme-address");
    }

    @Test
    public void shouldResolvePlaceholderInStructDef() {
        LHStructDefType type = new LHStructDefType(Address.class, LHTypeAdapterRegistry.empty(), PLACEHOLDERS);

        StructDef structDef = type.toStructDef();

        assertThat(structDef.getId().getName()).isEqualTo("acme-address");
    }

    @Test
    public void shouldResolvePlaceholderInNestedStructFieldReference() {
        LHStructDefType type = new LHStructDefType(Person.class, LHTypeAdapterRegistry.empty(), PLACEHOLDERS);

        InlineStructDef inlineStructDef = type.getInlineStructDef();

        StructDefId nestedStructDefId =
                inlineStructDef.getFieldsOrThrow("homeAddress").getFieldType().getStructDefId();

        assertThat(nestedStructDefId.getName()).isEqualTo("acme-address");
    }

    @Test
    public void shouldResolvePlaceholderInArrayOfStructFieldReference() {
        LHStructDefType type = new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty(), PLACEHOLDERS);

        InlineStructDef inlineStructDef = type.getInlineStructDef();

        TypeDefinition arrayElementType = inlineStructDef
                .getFieldsOrThrow("addresses")
                .getFieldType()
                .getInlineArrayDef()
                .getArrayType();

        assertThat(arrayElementType.getStructDefId().getName()).isEqualTo("acme-address");
    }

    @Test
    public void shouldLeaveLiteralNameUnchangedWhenPlaceholdersProvided() {
        LHStructDefType type = new LHStructDefType(PlainAddress.class, LHTypeAdapterRegistry.empty(), PLACEHOLDERS);

        assertThat(type.getStructDefId().getName()).isEqualTo("plain-address");
    }

    @Test
    public void shouldDefaultToNoPlaceholdersForLiteralNames() {
        LHStructDefType type = new LHStructDefType(PlainAddress.class, LHTypeAdapterRegistry.empty());

        assertThat(type.getStructDefId().getName()).isEqualTo("plain-address");
    }

    @Test
    public void shouldThrowWhenPlaceholderValueMissing() {
        LHStructDefType type = new LHStructDefType(Address.class, LHTypeAdapterRegistry.empty(), Map.of());

        assertThatThrownBy(type::getStructDefId)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("company");
    }

    @Test
    public void shouldResolvePlaceholderInDeclareStructWithClass() {
        Workflow wf = Workflow.newWorkflow(
                "placeholder-struct-wf", thread -> thread.declareStruct("my-address", Address.class), PLACEHOLDERS);

        VariableDef varDef = declaredVarDef(wf);

        assertThat(varDef.getTypeDef().getStructDefId().getName()).isEqualTo("acme-address");
    }

    @Test
    public void shouldResolvePlaceholderInDeclareStructWithStringName() {
        Workflow wf = Workflow.newWorkflow(
                "placeholder-struct-name-wf",
                thread -> thread.declareStruct("my-address", "${company}-address"),
                PLACEHOLDERS);

        VariableDef varDef = declaredVarDef(wf);

        assertThat(varDef.getTypeDef().getStructDefId().getName()).isEqualTo("acme-address");
    }

    private static VariableDef declaredVarDef(Workflow wf) {
        PutWfSpecRequest wfSpec = wf.compileWorkflow();
        return wfSpec.getThreadSpecsOrThrow(wfSpec.getEntrypointThreadName())
                .getVariableDefsList()
                .get(0)
                .getVarDef();
    }
}
