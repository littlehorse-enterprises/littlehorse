package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHStringAdapter;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStruct;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.Struct;
import io.littlehorse.sdk.common.proto.StructField;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.internal.taskdefutil.LHTaskSignature;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;
import io.littlehorse.sdk.worker.LHStructOneOf;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHTaskMethodHandle;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LHStructOneOfTest {
    @LHStructDef("payment-oneof")
    public record Payment(
            String orderId,
            @LHStructOneOf(group = "method", required = true) String cardToken,
            @LHStructOneOf(group = "method", required = true) String bankAccountId) {}

    @LHStructDef("renamed-oneof")
    public record Renamed(
            @LHStructOneOf(group = "choice") @LHStructField(name = "wireName") String javaName,
            @LHStructOneOf(group = "choice") String alternate) {}

    @LHStructDef("pojo-oneof")
    public static class Pojo {
        @LHStructOneOf(group = "choice", required = true)
        private String left;

        private String right;

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        @LHStructOneOf(group = "choice", required = true)
        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }
    }

    @LHStructDef("setter-oneof")
    public static class SetterTagged {
        private String left;
        private String right;

        public String getLeft() {
            return left;
        }

        @LHStructOneOf(group = "choice")
        public void setLeft(String left) {
            this.left = left;
        }

        public String getRight() {
            return right;
        }

        @LHStructOneOf(group = "choice")
        public void setRight(String right) {
            this.right = right;
        }
    }

    @LHStructDef("invalid-default-oneof")
    public static class Defaulted {
        @LHStructOneOf(group = "choice")
        private String left = "default";

        @LHStructOneOf(group = "choice")
        private String right;

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }
    }

    @LHStructDef("invalid-nullable-oneof")
    record Nullable(
            @LHStructOneOf(group = "choice") @LHStructField(isNullable = true) String left,
            @LHStructOneOf(group = "choice") String right) {}

    @LHStructDef("invalid-primitive-oneof")
    record Primitive(@LHStructOneOf(group = "choice") int left, @LHStructOneOf(group = "choice") String right) {}

    @LHStructDef("singleton-oneof")
    public record Singleton(@LHStructOneOf(group = "choice") String left, String right) {}

    @LHStructDef("invalid-conflicting-oneof")
    record Conflicting(
            @LHStructOneOf(group = "choice", required = true) String left,
            @LHStructOneOf(group = "choice") String right) {}

    @LHStructDef("invalid-missing-group")
    record MissingGroup(@LHStructOneOf String left, String right) {}

    @LHStructOneOf(group = "choice")
    public record TypeWithGroup(String left, String right) {}

    @LHStructDef("invalid-named-choice")
    @LHStructOneOf
    public record NamedChoice(String left, String right) {}

    @LHStructDef("invalid-named-choice-pojo")
    @LHStructOneOf
    public static class NamedChoicePojo extends ChoicePojo {}

    @LHStructDef("invalid-blank-oneof")
    record BlankGroup(@LHStructOneOf(group = " ") String left, @LHStructOneOf(group = " ") String right) {}

    @LHStructDef("invalid-duplicate-wire-name")
    record DuplicateName(
            @LHStructOneOf(group = "choice") @LHStructField(name = "same") String left,
            @LHStructOneOf(group = "choice") @LHStructField(name = "same") String right) {}

    @LHStructDef("boolean-oneof")
    public record BooleanChoice(
            @LHStructOneOf(group = "choice", required = true) Boolean enabled,
            @LHStructOneOf(group = "choice", required = true) String label) {}

    @LHStructDef("multiple-oneofs")
    public record MultipleGroups(
            @LHStructOneOf(group = "first", required = true) String a,
            @LHStructOneOf(group = "first", required = true) String b,
            @LHStructOneOf(group = "second") String c,
            @LHStructOneOf(group = "second") String d) {}

    @LHStructOneOf(required = true)
    public record Method(String cardToken, String bankAccountId) {}

    @LHStructDef("payment-oneof")
    public record NestedPayment(String orderId, Method method) {}

    @LHStructOneOf
    public record OptionalChoice(@LHStructField(name = "wireName") String javaName, String alternate) {}

    @LHStructDef("renamed-oneof")
    public record NestedOptional(OptionalChoice choice) {}

    @LHStructOneOf(required = true)
    public static class ChoicePojo {
        private String left;
        private String right;

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }
    }

    @LHStructDef("pojo-oneof")
    public record NestedPojo(ChoicePojo choice) {}

    @LHStructDef("invalid-collision")
    record Collision(
            String choice, @LHStructOneOf(group = "choice") String a, @LHStructOneOf(group = "choice") String b) {}

    @LHStructDef("invalid-group-name")
    record InvalidName(@LHStructOneOf(group = "bad-name") String a, @LHStructOneOf(group = "bad-name") String b) {}

    @LHStructOneOf
    public record Mixed(@LHStructOneOf(group = "choice") String a, @LHStructOneOf(group = "choice") String b) {}

    @LHStructOneOf
    public record SingletonChoice(String left) {}

    @LHStructDef("singleton-oneof")
    public record NestedSingleton(SingletonChoice choice, String right) {}

    @LHStructOneOf(required = true)
    public record RequiredSingletonChoice(String left) {}

    @LHStructDef("required-singleton-oneof")
    public record RequiredSingleton(@LHStructOneOf(group = "choice", required = true) String left) {}

    @LHStructDef("required-singleton-oneof")
    public record NestedRequiredSingleton(RequiredSingletonChoice choice) {}

    @LHStructOneOf
    public record EmptyChoice() {}

    @LHStructOneOf
    public record IgnoredChoice(@LHStructIgnore String ignored) {}

    @LHStructDef("optional-parent")
    public record OptionalParent(@LHStructField(isNullable = true) Method method) {}

    @LHStructDef("choice-collections")
    public record ChoiceCollections(Method[] methods, Map<String, Method> byKey) {}

    @LHStructOneOf(required = true)
    public record AdaptedChoice(UUID uuid, String label) {}

    @LHStructDef("${company}-address")
    public record Address(String street) {}

    @LHStructOneOf(required = true)
    public record AddressChoice(Address address, String label) {}

    @LHStructDef("placeholder-choice")
    public record PlaceholderChoice(AddressChoice method) {}

    @LHStructOneOf(required = true)
    public record ChoiceOfChoices(Method method, String label) {}

    @LHStructOneOf(required = true)
    public record ZeroChoice(Integer number, String label) {}

    @LHStructDef("metadata-choice")
    public record FlatMetadata(
            @LHStructOneOf(group = "choice") @LHStructField(masked = true, description = "Token") String token,
            @LHStructOneOf(group = "choice") String other,
            @LHStructOneOf(group = "choice") @LHStructIgnore String ignored) {}

    @LHStructOneOf
    public record MetadataChoice(
            @LHStructField(masked = true, description = "Token") String token,
            String other,
            @LHStructIgnore String ignored) {}

    @LHStructDef("metadata-choice")
    public record NestedMetadata(MetadataChoice choice) {}

    public static class ChoiceTasks {
        @LHTaskMethod("choice-task")
        public Method choose(Method method) {
            return method;
        }
    }

    @Test
    void bothApisGenerateExactlyTheSameNestedSchema() throws Exception {
        InlineStructDef flat = definition(Payment.class);
        InlineStructDef nested = definition(NestedPayment.class);
        assertThat(flat).isEqualTo(nested);
        assertThat(new LHStructDefType(Payment.class).toPutStructDefRequest())
                .isEqualTo(new LHStructDefType(NestedPayment.class).toPutStructDefRequest());
        assertThat(flat.getFieldsMap()).containsOnlyKeys("orderId", "method");
        InlineStructDef method = flat.getFieldsOrThrow("method").getFieldType().getInlineStructDef();
        assertThat(method.getFieldsMap()).containsOnlyKeys("cardToken", "bankAccountId");
        assertThat(method.getOneofConstraints().getSelectionRequired()).isTrue();
        assertThat(InlineStructDef.parseFrom(flat.toByteArray())).isEqualTo(flat);
    }

    @Test
    void bothApisGenerateIdenticalValuesAndCanReadEachOthersValues() throws Exception {
        Payment flat = new Payment("order-123", "tok_abc", null);
        NestedPayment nested = new NestedPayment("order-123", new Method("tok_abc", null));
        Struct flatWire = LHLibUtil.serializeToStruct(flat);
        Struct nestedWire = LHLibUtil.serializeToStruct(nested);
        assertThat(flatWire).isEqualTo(nestedWire);
        assertThat(flatWire.getStruct().getFieldsMap()).containsOnlyKeys("orderId", "method");
        assertThat(child(flatWire, "method").getStruct().getFieldsMap()).containsOnlyKeys("cardToken");
        assertThat(child(flatWire, "method").hasStructDefId()).isFalse();
        assertThat(LHLibUtil.varValToObj(value(Struct.parseFrom(nestedWire.toByteArray())), Payment.class))
                .isEqualTo(flat);
        assertThat(LHLibUtil.varValToObj(value(flatWire), NestedPayment.class)).isEqualTo(nested);

        Payment bank = new Payment("order-123", null, "bank-123");
        assertThat(LHLibUtil.varValToObj(value(LHLibUtil.serializeToStruct(bank)), NestedPayment.class))
                .isEqualTo(new NestedPayment("order-123", new Method(null, "bank-123")));
    }

    @Test
    void emptyChoicesAlwaysEmitAnEmptyContainerAndPreserveFalseConstraintPresence() {
        Renamed flat = new Renamed(null, null);
        NestedOptional nested = new NestedOptional(new OptionalChoice(null, null));
        assertThat(definition(Renamed.class)).isEqualTo(definition(NestedOptional.class));
        InlineStructDef schema = definition(Renamed.class)
                .getFieldsOrThrow("choice")
                .getFieldType()
                .getInlineStructDef();
        assertThat(schema.hasOneofConstraints()).isTrue();
        assertThat(schema.getOneofConstraints().getSelectionRequired()).isFalse();
        assertThat(schema.getFieldsMap()).containsOnlyKeys("wireName", "alternate");
        Struct wire = LHLibUtil.serializeToStruct(flat);
        assertThat(wire).isEqualTo(LHLibUtil.serializeToStruct(nested));
        assertThat(wire.getStruct().getFieldsMap()).containsOnlyKeys("choice");
        assertThat(child(wire, "choice").getStruct().getFieldsMap()).isEmpty();
        assertThat(LHLibUtil.varValToObj(value(wire), Renamed.class)).isEqualTo(flat);
        assertThat(LHLibUtil.varValToObj(value(wire), NestedOptional.class)).isEqualTo(nested);
    }

    @Test
    void renamedAlternativesRoundTripAcrossApis() {
        Struct wire = LHLibUtil.serializeToStruct(new Renamed("email", null));
        assertThat(child(wire, "choice").getStruct().getFieldsMap()).containsOnlyKeys("wireName");
        assertThat(LHLibUtil.varValToObj(value(wire), NestedOptional.class))
                .isEqualTo(new NestedOptional(new OptionalChoice("email", null)));
    }

    @Test
    void pojoFieldAndGetterAnnotationsRoundTripAcrossApis() {
        Pojo flat = new Pojo();
        flat.setRight("bank-123");
        ChoicePojo choice = new ChoicePojo();
        choice.setRight("bank-123");
        assertThat(definition(Pojo.class)).isEqualTo(definition(NestedPojo.class));
        Struct wire = LHLibUtil.serializeToStruct(flat);
        assertThat(wire).isEqualTo(LHLibUtil.serializeToStruct(new NestedPojo(choice)));
        Pojo decoded = (Pojo) LHLibUtil.varValToObj(value(wire), Pojo.class);
        assertThat(decoded.getLeft()).isNull();
        assertThat(decoded.getRight()).isEqualTo("bank-123");
        NestedPojo nested = (NestedPojo) LHLibUtil.varValToObj(value(wire), NestedPojo.class);
        assertThat(nested.choice().getLeft()).isNull();
        assertThat(nested.choice().getRight()).isEqualTo("bank-123");
    }

    @Test
    void discoversSetterAnnotations() {
        InlineStructDef child = definition(SetterTagged.class)
                .getFieldsOrThrow("choice")
                .getFieldType()
                .getInlineStructDef();
        assertThat(child.getFieldsMap()).containsOnlyKeys("left", "right");
    }

    @Test
    void zeroAndFalseAreSelections() {
        Struct wire = LHLibUtil.serializeToStruct(new BooleanChoice(false, null));
        assertThat(child(wire, "choice").getStruct().getFieldsMap()).containsOnlyKeys("enabled");
        assertThat(child(wire, "choice")
                        .getStruct()
                        .getFieldsOrThrow("enabled")
                        .getValue()
                        .getBool())
                .isFalse();
        assertThat(LHLibUtil.varValToObj(value(wire), BooleanChoice.class)).isEqualTo(new BooleanChoice(false, null));
        for (ZeroChoice input : new ZeroChoice[] {new ZeroChoice(0, null), new ZeroChoice(null, "")}) {
            VariableValue encoded = LHLibUtil.objToVarVal(input);
            assertThat(encoded.getStruct().getStruct().getFieldsCount()).isEqualTo(1);
            assertThat(LHLibUtil.varValToObj(encoded, ZeroChoice.class)).isEqualTo(input);
        }
    }

    @Test
    void independentGroupsKeepSeparateContainers() {
        MultipleGroups input = new MultipleGroups("a", null, null, "d");
        Struct wire = LHLibUtil.serializeToStruct(input);
        assertThat(wire.getStruct().getFieldsMap()).containsOnlyKeys("first", "second");
        assertThat(child(wire, "first").getStruct().getFieldsMap()).containsOnlyKeys("a");
        assertThat(child(wire, "second").getStruct().getFieldsMap()).containsOnlyKeys("d");
        assertThat(LHLibUtil.varValToObj(value(wire), MultipleGroups.class)).isEqualTo(input);
        assertThatThrownBy(() -> LHLibUtil.serializeToStruct(new MultipleGroups("a", null, "c", "d")))
                .hasMessageContaining("second requires at most one field");
        assertThatThrownBy(() -> LHLibUtil.serializeToStruct(new MultipleGroups(null, null, "c", null)))
                .hasMessageContaining("first requires exactly one field");
    }

    @Test
    void rejectsInvalidLocalSelectionsInBothApis() {
        for (Object invalid : new Object[] {
            new Payment("order", null, null),
            new Payment("order", "a", "b"),
            new NestedPayment("order", new Method(null, null)),
            new NestedPayment("order", new Method("a", "b"))
        }) {
            assertThatThrownBy(() -> LHLibUtil.serializeToStruct(invalid))
                    .hasMessageContaining("requires exactly one field");
        }
        assertThatThrownBy(() -> LHLibUtil.serializeToStruct(new NestedPayment("order", null)))
                .hasMessageContaining("Required oneof field method");
    }

    @Test
    void rejectsMultipleNullAndUnknownWireAlternativesInBothApis() {
        Struct wire = LHLibUtil.serializeToStruct(new Payment("order", "token", null));
        Struct method = child(wire, "method");
        Struct multiple = method.toBuilder()
                .setStruct(method.getStruct().toBuilder()
                        .putFields(
                                "bankAccountId",
                                field(VariableValue.newBuilder().setStr("bank").build())))
                .build();
        Struct explicitNull = method.toBuilder()
                .setStruct(method.getStruct().toBuilder()
                        .putFields("cardToken", field(VariableValue.getDefaultInstance())))
                .build();
        Struct unknown = method.toBuilder()
                .setStruct(method.getStruct().toBuilder()
                        .putFields(
                                "unknown",
                                field(VariableValue.newBuilder().setStr("x").build())))
                .build();
        for (Class<?> target : new Class<?>[] {Payment.class, NestedPayment.class}) {
            assertThatThrownBy(() -> LHLibUtil.varValToObj(value(withChild(wire, "method", multiple)), target))
                    .hasMessageContaining("requires exactly one field");
            assertThatThrownBy(() -> LHLibUtil.varValToObj(value(withChild(wire, "method", explicitNull)), target))
                    .hasMessageContaining("cannot be null");
            assertThatThrownBy(() -> LHLibUtil.varValToObj(value(withChild(wire, "method", unknown)), target))
                    .hasMessageContaining("Unknown field unknown");
        }
    }

    @Test
    void rejectsMissingMalformedAndLegacyFlatContainers() {
        Struct wire = LHLibUtil.serializeToStruct(new Payment("order", "token", null));
        Struct missing = wire.toBuilder()
                .setStruct(wire.getStruct().toBuilder().removeFields("method"))
                .build();
        Struct malformed = wire.toBuilder()
                .setStruct(wire.getStruct().toBuilder()
                        .putFields(
                                "method",
                                field(VariableValue.newBuilder().setStr("token").build())))
                .build();
        Struct legacy = wire.toBuilder()
                .setStruct(wire.getStruct().toBuilder()
                        .putFields(
                                "cardToken",
                                field(VariableValue.newBuilder().setStr("token").build())))
                .build();
        assertThatThrownBy(() -> LHLibUtil.varValToObj(value(missing), Payment.class))
                .hasMessageContaining("nested inline Struct");
        assertThatThrownBy(() -> LHLibUtil.varValToObj(value(malformed), Payment.class))
                .hasMessageContaining("nested inline Struct");
        assertThatThrownBy(() -> LHLibUtil.varValToObj(value(legacy), Payment.class))
                .hasMessageContaining("must be nested under method");
    }

    @Test
    void optionalParentCanBeAbsentButPresentChoiceMustValidate() {
        Struct absent =
                Struct.newBuilder().setStruct(InlineStruct.getDefaultInstance()).build();
        assertThat(LHLibUtil.varValToObj(value(absent), OptionalParent.class)).isEqualTo(new OptionalParent(null));
        OptionalParent input = new OptionalParent(new Method("token", null));
        assertThat(LHLibUtil.varValToObj(value(LHLibUtil.serializeToStruct(input)), OptionalParent.class))
                .isEqualTo(input);
        assertThatThrownBy(() -> LHLibUtil.serializeToStruct(new OptionalParent(new Method(null, null))))
                .hasMessageContaining("requires exactly one field");
    }

    @Test
    void choiceTypesWorkAtTheRootInArraysAndInMaps() {
        Method choice = new Method("token", null);
        VariableValue root = LHLibUtil.objToVarVal(choice);
        assertThat(root.getStruct().hasStructDefId()).isFalse();
        assertThat(LHClassType.fromJavaClass(Method.class, LHTypeAdapterRegistry.empty())
                        .getTypeDefinition()
                        .getInlineStructDef()
                        .hasOneofConstraints())
                .isTrue();
        assertThat(LHLibUtil.varValToObj(root, Method.class)).isEqualTo(choice);
        ChoiceCollections input = new ChoiceCollections(new Method[] {choice}, Map.of("first", choice));
        ChoiceCollections decoded = (ChoiceCollections)
                LHLibUtil.varValToObj(value(LHLibUtil.serializeToStruct(input)), ChoiceCollections.class);
        assertThat(decoded.methods()).containsExactly(choice);
        assertThat(decoded.byKey()).isEqualTo(input.byKey());
    }

    @Test
    void preservesAdaptersAndNamedDependenciesInsideChoices() {
        LHStringAdapter<UUID> uuidAdapter = new LHStringAdapter<UUID>() {
            public Class<UUID> getTypeClass() {
                return UUID.class;
            }

            public String toString(UUID value) {
                return value.toString();
            }

            public UUID fromString(String value) {
                return UUID.fromString(value);
            }
        };
        LHTypeAdapterRegistry registry = LHTypeAdapterRegistry.from(Map.of(UUID.class, uuidAdapter));
        AdaptedChoice input = new AdaptedChoice(UUID.randomUUID(), null);
        VariableValue wire = LHLibUtil.objToVarVal(input, registry);
        assertThat(LHLibUtil.varValToObj(wire, AdaptedChoice.class, registry)).isEqualTo(input);
        PlaceholderChoice holder = new PlaceholderChoice(new AddressChoice(new Address("Main"), null));
        Struct encoded = LHLibUtil.serializeToStruct(holder, registry, Map.of("company", "acme"));
        assertThat(child(child(encoded, "method"), "address").getStructDefId().getName())
                .isEqualTo("acme-address");
        assertThat(LHLibUtil.varValToObj(value(encoded), PlaceholderChoice.class, registry, Map.of("company", "acme")))
                .isEqualTo(holder);
    }

    @Test
    void choicesCanBeUnselectedAlternativesOfAnotherChoice() {
        for (ChoiceOfChoices input : new ChoiceOfChoices[] {
            new ChoiceOfChoices(null, "label"), new ChoiceOfChoices(new Method("token", null), null)
        }) {
            assertThat(LHLibUtil.varValToObj(LHLibUtil.objToVarVal(input), ChoiceOfChoices.class))
                    .isEqualTo(input);
        }
    }

    @Test
    void preservesMaskingDescriptionsAndIgnoredFieldsAcrossLayouts() {
        assertThat(definition(FlatMetadata.class)).isEqualTo(definition(NestedMetadata.class));
        var choice = definition(FlatMetadata.class)
                .getFieldsOrThrow("choice")
                .getFieldType()
                .getInlineStructDef();
        assertThat(choice.getFieldsMap()).containsOnlyKeys("token", "other");
        assertThat(choice.getFieldsOrThrow("token").getFieldType().getMasked()).isTrue();
        assertThat(choice.getFieldsOrThrow("token").getDescription()).isEqualTo("Token");
        var flat = new FlatMetadata("token", null, "ignored");
        var nested = new NestedMetadata(new MetadataChoice("token", null, "ignored"));
        assertThat(LHLibUtil.objToVarVal(flat)).isEqualTo(LHLibUtil.objToVarVal(nested));
        assertThat(LHLibUtil.varValToObj(LHLibUtil.objToVarVal(flat), NestedMetadata.class))
                .isEqualTo(new NestedMetadata(new MetadataChoice("token", null, null)));
    }

    @Test
    void taskInputsAndResultsUseInlineChoiceMetadata() throws Exception {
        var method = ChoiceTasks.class.getMethod("choose", Method.class);
        var signature = new LHTaskSignature(
                LHTaskMethodHandle.fromLHTaskMethod(method, Map.of()), LHTypeAdapterRegistry.empty(), Map.of());
        var request = signature.toPutTaskDefRequest();
        var inputType = request.getInputVars(0).getTypeDef();
        assertThat(inputType.getInlineStructDef().getOneofConstraints().getSelectionRequired())
                .isTrue();
        assertThat(request.getReturnType().getReturnType()).isEqualTo(inputType);
    }

    @Test
    void namedStructsCannotBeWholeTypeChoices() {
        for (Object choice : new Object[] {new NamedChoice("left", null), new NamedChoicePojo()}) {
            assertThatThrownBy(() -> definition(choice.getClass()))
                    .hasMessageContaining("Cannot combine type-level @LHStructOneOf with @LHStructDef");
            assertThatThrownBy(() -> LHLibUtil.objToVarVal(choice))
                    .hasMessageContaining("Cannot combine type-level @LHStructOneOf with @LHStructDef");
        }
    }

    @Test
    void singleAlternativeGroupsRoundTripAcrossBothApis() {
        assertThat(definition(Singleton.class)).isEqualTo(definition(NestedSingleton.class));
        for (String selected : new String[] {null, "left"}) {
            var flat = new Singleton(selected, "ordinary field");
            var nested = new NestedSingleton(new SingletonChoice(selected), "ordinary field");
            VariableValue wire = LHLibUtil.objToVarVal(flat);
            assertThat(wire).isEqualTo(LHLibUtil.objToVarVal(nested));
            assertThat(LHLibUtil.varValToObj(wire, Singleton.class)).isEqualTo(flat);
            assertThat(LHLibUtil.varValToObj(wire, NestedSingleton.class)).isEqualTo(nested);
        }
    }

    @Test
    void requiredSingleAlternativeGroupsStillRequireSelection() {
        assertThat(definition(RequiredSingleton.class)).isEqualTo(definition(NestedRequiredSingleton.class));
        var flat = new RequiredSingleton("left");
        var nested = new NestedRequiredSingleton(new RequiredSingletonChoice("left"));
        VariableValue wire = LHLibUtil.objToVarVal(flat);
        assertThat(wire).isEqualTo(LHLibUtil.objToVarVal(nested));
        assertThat(LHLibUtil.varValToObj(wire, RequiredSingleton.class)).isEqualTo(flat);
        assertThat(LHLibUtil.varValToObj(wire, NestedRequiredSingleton.class)).isEqualTo(nested);
        for (Object invalid : new Object[] {
            new RequiredSingleton(null), new NestedRequiredSingleton(new RequiredSingletonChoice(null))
        }) {
            assertThatThrownBy(() -> LHLibUtil.objToVarVal(invalid)).hasMessageContaining("requires exactly one field");
        }
        Struct empty = LHLibUtil.serializeToStruct(new NestedRequiredSingleton(new RequiredSingletonChoice("left")));
        VariableValue missingSelection = value(withChild(
                empty,
                "choice",
                Struct.newBuilder().setStruct(InlineStruct.getDefaultInstance()).build()));
        for (Class<?> target : new Class<?>[] {RequiredSingleton.class, NestedRequiredSingleton.class}) {
            assertThatThrownBy(() -> LHLibUtil.varValToObj(missingSelection, target))
                    .hasMessageContaining("requires exactly one field");
        }
    }

    @Test
    void rejectsInvalidDefinitions() {
        assertThatThrownBy(() -> definition(Defaulted.class)).hasMessageContaining("without a default");
        assertThatThrownBy(() -> definition(Nullable.class)).hasMessageContaining("without a default");
        assertThatThrownBy(() -> definition(Primitive.class)).hasMessageContaining("reference type");
        assertThatThrownBy(() -> definition(Conflicting.class)).hasMessageContaining("conflicting required");
        assertThatThrownBy(() -> definition(BlankGroup.class)).hasMessageContaining("nonblank");
        assertThatThrownBy(() -> definition(MissingGroup.class)).hasMessageContaining("nonblank");
        assertThatThrownBy(() -> LHClassType.fromJavaClass(TypeWithGroup.class, LHTypeAdapterRegistry.empty()))
                .hasMessageContaining("group must be omitted on type");
        assertThatThrownBy(() -> definition(DuplicateName.class)).hasMessageContaining("exactly one Java property");
        assertThatThrownBy(() -> definition(Collision.class))
                .hasMessageContaining("conflicts with a serialized property");
        assertThatThrownBy(() -> definition(InvalidName.class)).hasMessageContaining("valid Struct field name");
        assertThatThrownBy(() -> LHClassType.fromJavaClass(Mixed.class, LHTypeAdapterRegistry.empty()))
                .hasMessageContaining("Cannot combine");
        for (Class<?> type : new Class<?>[] {EmptyChoice.class, IgnoredChoice.class}) {
            assertThatThrownBy(() -> LHClassType.fromJavaClass(type, LHTypeAdapterRegistry.empty()))
                    .hasMessageContaining("at least one field");
        }
    }

    private static InlineStructDef definition(Class<?> type) {
        return new LHStructDefType(type).getInlineStructDef();
    }

    private static VariableValue value(Struct struct) {
        return VariableValue.newBuilder().setStruct(struct).build();
    }

    private static StructField field(VariableValue value) {
        return StructField.newBuilder().setValue(value).build();
    }

    private static Struct child(Struct parent, String name) {
        return parent.getStruct().getFieldsOrThrow(name).getValue().getStruct();
    }

    private static Struct withChild(Struct parent, String name, Struct child) {
        return parent.toBuilder()
                .setStruct(parent.getStruct().toBuilder().putFields(name, field(value(child))))
                .build();
    }
}
