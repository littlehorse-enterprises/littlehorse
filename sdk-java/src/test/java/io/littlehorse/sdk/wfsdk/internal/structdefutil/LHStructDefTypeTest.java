package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;
import java.util.stream.Stream;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LHStructDefTypeTest {

    @LHStructDef("book")
    @Getter
    class Book {
        public String title;
        public int numPages;
        public Author author;
    }

    @LHStructDef("authorFieldsOnly")
    class AuthorFieldsOnly {
        public String name;
        public int age;
        public boolean isAlive;
        public double heightCm;
        public byte[] bytes;
        public WfRunId wfRunId;
    }

    @LHStructDef("author")
    @Getter
    class Author {
        public String name;
        public int age;
        public boolean isAlive;
        public double heightCm;
        public byte[] bytes;
        public WfRunId wfRunId;
    }

    @LHStructDef("author")
    record AuthorRecord(String name, int age, boolean alive, double heightCm, byte[] bytes, WfRunId wfRunId) {}

    @LHStructDef("book")
    record BookRecord(String title, int numPages, Author author) {}

    @LHStructDef("library")
    class Library {
        public String name;

        public Book[] books;

        public String getName() {
            return this.name;
        }

        public Book[] getBooks() {
            return this.books;
        }
    }

    @LHStructDef("library")
    record LibraryRecord(String name, Book[] books) {}

    @Getter
    @LHStructDef("maskedValueDemo")
    class MaskedValueDemo {
        public String maskedValue;

        @LHStructField(masked = true)
        public String getMaskedValue() {
            return this.maskedValue;
        }
    }

    @LHStructDef("maskedValueDemo")
    record MaskedValueRecord(@LHStructField(masked = true) String maskedValue) {}

    @LHStructDef("named-field-demo")
    class NamedFieldDemo {
        public int inferredFieldName;

        @LHStructField(name = "customFieldName")
        public int getInferredFieldName() {
            return this.inferredFieldName;
        }
    }

    @LHStructDef("named-field-demo")
    record NamedFieldRecord(@LHStructField(name = "customFieldName") int inferredFieldName) {}

    @LHStructDef("field-annotation-demo")
    @Getter
    class FieldAnnotationDemo {
        @LHStructField(masked = true)
        public String secret;

        @LHStructField(name = "publicLabel", isNullable = true)
        public String displayName;
    }

    @LHStructDef("field-annotation-demo")
    record FieldAnnotationRecord(
            @LHStructField(masked = true) String secret,
            @LHStructField(name = "publicLabel", isNullable = true) String displayName) {}

    @LHStructDef("described-field-demo")
    @Getter
    class DescribedFieldDemo {
        @LHStructField(description = "The user's primary contact email")
        public String email;

        public String name;
    }

    @LHStructDef("described-field-demo")
    record DescribedFieldRecord(
            @LHStructField(description = "The user's primary contact email") String email, String name) {}

    @LHStructDef("ignored-field-demo")
    @Getter
    class IgnoredFieldDemo {
        public String included;
        public String ignored;

        @LHStructIgnore
        public String getIgnored() {
            return ignored;
        }
    }

    @LHStructDef("ignored-field-demo")
    record IgnoredFieldRecord(String included, @LHStructIgnore String ignored) {}

    @LHStructDef("boolean-field-annotation-demo")
    @Getter
    class BooleanFieldAnnotationDemo {
        @LHStructField(name = "isPersonAlive")
        public boolean isAlive;
    }

    @LHStructDef("boolean-field-annotation-demo")
    record BooleanFieldAnnotationRecord(@LHStructField(name = "isPersonAlive") boolean alive) {}

    @LHStructDef("annotated-person")
    class AnnotatedPersonDemo {
        private String name;
        private String ssn;

        @LHStructField(name = "fullName", isNullable = true)
        public String getName() {
            return name;
        }

        @LHStructField(masked = true)
        public String getSsn() {
            return ssn;
        }
    }

    @LHStructDef("annotated-person")
    record AnnotatedPersonRecord(String name, String ssn) {
        @Override
        @LHStructField(name = "fullName", isNullable = true)
        public String name() {
            return name;
        }

        @Override
        @LHStructField(masked = true)
        public String ssn() {
            return ssn;
        }
    }

    @Getter
    class UnannotatedNestedPojo {
        public String value;
    }

    @Getter
    class InlinePojoWithNamedDependency {
        public Author author;
    }

    @LHStructDef("invalid-json-obj-holder")
    @Getter
    class InvalidJsonObjHolder {
        public UnannotatedNestedPojo nestedPojo;
    }

    @LHStructDef("invalid-json-obj-holder")
    record InlineRecordHolder(UnannotatedNestedPojo nestedPojo) {}

    @LHStructDef("inline-struct-array-holder")
    @Getter
    class InlineStructArrayHolder {
        public UnannotatedNestedPojo[] nestedPojos;
    }

    @LHStructDef("inline-struct-array-holder")
    record InlineStructArrayRecordHolder(UnannotatedNestedPojo[] nestedPojos) {}

    @LHStructDef("invalid-json-arr-holder")
    @Getter
    class InvalidJsonArrHolder {
        public java.util.List<String> names;
    }

    @LHStructDef("invalid-json-arr-holder")
    record InvalidJsonArrRecordHolder(java.util.List<String> names) {}

    @LHStructDef("inline-dependency-holder")
    @Getter
    class InlineDependencyHolder {
        private InlinePojoWithNamedDependency nestedPojo;
    }

    @LHStructDef("inline-dependency-holder")
    record InlineDependencyRecordHolder(InlinePojoWithNamedDependency nestedPojo) {}

    static Stream<Arguments> structDefinitionParityTypes() {
        return Stream.of(
                Arguments.of("primitive fields", Author.class, AuthorRecord.class),
                Arguments.of("named struct fields", Book.class, BookRecord.class),
                Arguments.of("arrays of named structs", Library.class, LibraryRecord.class),
                Arguments.of("masked fields", MaskedValueDemo.class, MaskedValueRecord.class),
                Arguments.of("custom field names", NamedFieldDemo.class, NamedFieldRecord.class),
                Arguments.of("field annotations", FieldAnnotationDemo.class, FieldAnnotationRecord.class),
                Arguments.of("accessor annotations", AnnotatedPersonDemo.class, AnnotatedPersonRecord.class),
                Arguments.of("ignored fields", IgnoredFieldDemo.class, IgnoredFieldRecord.class),
                Arguments.of(
                        "boolean field annotations",
                        BooleanFieldAnnotationDemo.class,
                        BooleanFieldAnnotationRecord.class),
                Arguments.of("field descriptions", DescribedFieldDemo.class, DescribedFieldRecord.class),
                Arguments.of("inline structs", InvalidJsonObjHolder.class, InlineRecordHolder.class),
                Arguments.of(
                        "arrays of inline structs", InlineStructArrayHolder.class, InlineStructArrayRecordHolder.class),
                Arguments.of(
                        "nested named dependencies", InlineDependencyHolder.class, InlineDependencyRecordHolder.class));
    }

    @Test
    public void getEmptyInlineStructDefWhenClassDoesNotHaveGettersOrSetters() {
        LHStructDefType authorClassType = new LHStructDefType(AuthorFieldsOnly.class, LHTypeAdapterRegistry.empty());
        InlineStructDef actualInlineStructDef = authorClassType.getInlineStructDef();

        assertThat(actualInlineStructDef.toString()).isBlank();
    }

    @Test
    public void getInlineStructDefWithPrimitiveFields() {
        LHStructDefType authorClassType = new LHStructDefType(Author.class, LHTypeAdapterRegistry.empty());
        InlineStructDef actualInlineStructDef = authorClassType.getInlineStructDef();
        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "name",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .build())
                .putFields(
                        "age",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                .build())
                .putFields(
                        "alive",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL))
                                .build())
                .putFields(
                        "heightCm",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.DOUBLE))
                                .build())
                .putFields(
                        "bytes",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.BYTES))
                                .build())
                .putFields(
                        "wfRunId",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.WF_RUN_ID))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefWithFieldReferenceToAnotherStructDef() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(Book.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();
        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "title",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .build())
                .putFields(
                        "numPages",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                .build())
                .putFields(
                        "author",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setStructDefId(StructDefId.newBuilder()
                                                .setName("author")
                                                .setVersion(-1)))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefIgnoresFieldsWithLHStructIgnore() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();
        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "name",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .build())
                .putFields(
                        "books",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setInlineArrayDef(InlineArrayDef.newBuilder()
                                                .setArrayType(TypeDefinition.newBuilder()
                                                        .setStructDefId(StructDefId.newBuilder()
                                                                .setName("book")
                                                                .setVersion(-1)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefMarksFieldsAsMasked() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(MaskedValueDemo.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();
        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "maskedValue",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .setMasked(true))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefUsesAnnotatedFieldName() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(NamedFieldDemo.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();
        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "customFieldName",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefUsesLHStructFieldAnnotationOnClassFields() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(FieldAnnotationDemo.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();
        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "secret",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .setMasked(true))
                                .build())
                .putFields(
                        "publicLabel",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .setIsNullable(true)
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefResolvesBooleanIsPrefixFieldAnnotations() {
        InlineStructDef actualInlineStructDef = new LHStructDefType(
                        BooleanFieldAnnotationDemo.class, LHTypeAdapterRegistry.empty())
                .getInlineStructDef();
        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "isPersonAlive",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getStructDefTypeDefinition() {
        LHClassType structDefType = LHClassType.fromJavaClass(Author.class, LHTypeAdapterRegistry.empty());

        TypeDefinition actualTypeDefinition = structDefType.getTypeDefinition();
        TypeDefinition expectedTypeDefinition = TypeDefinition.newBuilder()
                .setStructDefId(StructDefId.newBuilder().setName("author").setVersion(-1))
                .build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    public void shouldInlineUnannotatedNestedPojo() {
        InlineStructDef definition =
                new LHStructDefType(InvalidJsonObjHolder.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();

        assertThat(definition.getFieldsOrThrow("nestedPojo").getFieldType().getInlineStructDef())
                .isEqualTo(InlineStructDef.newBuilder()
                        .putFields(
                                "value",
                                StructFieldDef.newBuilder()
                                        .setFieldType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                        .build())
                        .build());
    }

    @Test
    public void shouldBuildInlineArrayDefOfInlineStructDefs() {
        TypeDefinition fieldType = new LHStructDefType(InlineStructArrayHolder.class, LHTypeAdapterRegistry.empty())
                .getInlineStructDef()
                .getFieldsOrThrow("nestedPojos")
                .getFieldType();

        assertThat(fieldType.getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_ARRAY_DEF);
        TypeDefinition elementType = fieldType.getInlineArrayDef().getArrayType();
        assertThat(elementType.getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_STRUCT_DEF);
        assertThat(elementType
                        .getInlineStructDef()
                        .getFieldsOrThrow("value")
                        .getFieldType()
                        .getPrimitiveType())
                .isEqualTo(VariableType.STR);
    }

    @Test
    public void shouldRetainJsonObjectFallbackOutsideStructDefFields() {
        assertThat(LHClassType.fromJavaClass(UnannotatedNestedPojo.class, LHTypeAdapterRegistry.empty())
                        .getTypeDefinition()
                        .getPrimitiveType())
                .isEqualTo(VariableType.JSON_OBJ);
    }

    @Test
    public void shouldCollectNamedDependenciesNestedInsideInlinePojos() {
        assertThat(new LHStructDefType(InlineDependencyHolder.class, LHTypeAdapterRegistry.empty())
                        .getDependencyClasses())
                .extracting(dependency -> dependency.getStructDefId().getName())
                .containsExactly("author", "inline-dependency-holder");
    }

    @ParameterizedTest
    @MethodSource("invalidJsonArrayTypes")
    public void shouldRejectStructDefFieldResolvingToJsonArr(Class<?> structDefType) {
        assertThatThrownBy(() -> new LHStructDefType(structDefType, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Forbidden JSON type: JSON_ARR");
    }

    static Stream<Class<?>> invalidJsonArrayTypes() {
        return Stream.of(InvalidJsonArrHolder.class, InvalidJsonArrRecordHolder.class);
    }

    @LHStructDef("config-with-default-ux")
    public record ConfigWithDefaultUx(String mode, int retries) {
        public ConfigWithDefaultUx() {
            this("standard", 3);
        }
    }

    @LHStructDef("config-no-default-ux")
    public record ConfigNoDefaultUx(String mode, int retries) {}

    @Test
    void shouldOnlyComputeRecordDefaultValuesFromNoArgConstructor() {
        InlineStructDef withDefaults =
                new LHStructDefType(ConfigWithDefaultUx.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();
        InlineStructDef withoutDefaults =
                new LHStructDefType(ConfigNoDefaultUx.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();

        assertThat(withDefaults.getFieldsOrThrow("mode").getDefaultValue().getStr())
                .isEqualTo("standard");
        assertThat(withDefaults.getFieldsOrThrow("retries").getDefaultValue().getInt())
                .isEqualTo(3);
        assertThat(withoutDefaults.getFieldsOrThrow("mode").hasDefaultValue()).isFalse();
        assertThat(withoutDefaults.getFieldsOrThrow("retries").hasDefaultValue())
                .isFalse();
    }

    @Test
    public void getInlineStructDefSetsDescriptionFromLHStructFieldAnnotation() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(DescribedFieldDemo.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();

        assertThat(actualInlineStructDef.getFieldsMap().get("email").getDescription())
                .isEqualTo("The user's primary contact email");
        assertThat(actualInlineStructDef.getFieldsMap().get("name").hasDescription())
                .isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("structDefinitionParityTypes")
    public void recordsProduceTheSameStructDefinitionsAsClasses(
            String scenario, Class<?> classType, Class<?> recordType) {
        LHStructDefType classStructDef = new LHStructDefType(classType, LHTypeAdapterRegistry.empty());
        LHStructDefType recordStructDef = new LHStructDefType(recordType, LHTypeAdapterRegistry.empty());

        assertThat(recordStructDef.getInlineStructDef()).isEqualTo(classStructDef.getInlineStructDef());
        assertThat(recordStructDef.getTypeDefinition()).isEqualTo(classStructDef.getTypeDefinition());
        assertThat(recordStructDef.getDependencyClasses())
                .extracting(LHStructDefType::getStructDefId)
                .containsExactlyElementsOf(classStructDef.getDependencyClasses().stream()
                        .map(LHStructDefType::getStructDefId)
                        .toList());
    }
}
