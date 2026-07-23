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
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import lombok.Getter;
import org.junit.jupiter.api.Test;

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

    @Getter
    @LHStructDef("maskedValueDemo")
    class MaskedValueDemo {
        public String maskedValue;

        @LHStructField(masked = true)
        public String getMaskedValue() {
            return this.maskedValue;
        }
    }

    @LHStructDef("named-field-demo")
    class NamedFieldDemo {
        public int inferredFieldName;

        @LHStructField(name = "customFieldName")
        public int getInferredFieldName() {
            return this.inferredFieldName;
        }
    }

    @LHStructDef("field-annotation-demo")
    @Getter
    class FieldAnnotationDemo {
        @LHStructField(masked = true)
        public String secret;

        @LHStructField(name = "publicLabel")
        public String displayName;
    }

    @LHStructDef("boolean-field-annotation-demo")
    @Getter
    class BooleanFieldAnnotationDemo {
        @LHStructField(name = "isPersonAlive")
        public boolean isAlive;
    }

    @LHStructDef("person-record")
    record PersonRecord(String name, String address) {}

    @LHStructDef("annotated-person-record")
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

    @LHStructDef("record-with-default")
    record RecordWithDefaultCtor(String greeting) {
        public RecordWithDefaultCtor() {
            this("hello");
        }
    }

    @LHStructDef("component-annotated-record")
    record ComponentAnnotatedRecord(
            @LHStructField(name = "displayName", isNullable = true) String name,
            @LHStructField(masked = true) String secret) {}

    class UnannotatedNestedPojo {
        public String value;
    }

    @LHStructDef("invalid-json-obj-holder")
    @Getter
    class InvalidJsonObjHolder {
        public UnannotatedNestedPojo nestedPojo;
    }

    @LHStructDef("invalid-json-arr-holder")
    @Getter
    class InvalidJsonArrHolder {
        public java.util.List<String> names;
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
    public void getInlineStructDefFromRecordComponents() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(PersonRecord.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();

        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "name",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .build())
                .putFields(
                        "address",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefFromRecordAccessorAnnotations() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(AnnotatedPersonRecord.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();

        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "fullName",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .setIsNullable(true)
                                .build())
                .putFields(
                        "ssn",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .setMasked(true))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefFromRecordComponentAnnotations() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(ComponentAnnotatedRecord.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();

        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "displayName",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .setIsNullable(true)
                                .build())
                .putFields(
                        "secret",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .setMasked(true))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefFromRecordWithNoArgCtorIncludesDefaultValue() {
        InlineStructDef actualInlineStructDef =
                new LHStructDefType(RecordWithDefaultCtor.class, LHTypeAdapterRegistry.empty()).getInlineStructDef();

        StructFieldDef expectedFieldDef = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                .setDefaultValue(VariableValue.newBuilder().setStr("hello"))
                .build();

        assertThat(actualInlineStructDef.getFieldsMap().get("greeting")).isEqualTo(expectedFieldDef);
    }

    @Test
    public void shouldRejectStructDefFieldResolvingToJsonObj() {
        assertThatThrownBy(() -> new LHStructDefType(InvalidJsonObjHolder.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Forbidden JSON type: JSON_OBJ");
    }

    @Test
    public void shouldRejectStructDefFieldResolvingToJsonArr() {
        assertThatThrownBy(() -> new LHStructDefType(InvalidJsonArrHolder.class, LHTypeAdapterRegistry.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Forbidden JSON type: JSON_ARR");
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
    void shouldComputeDefaultValuesFromNoArgConstructor() {
        assertThat(ConfigWithDefaultUx.class.getDeclaredConstructors()).hasSize(2); // canonical + no-arg
        assertThat(ConfigNoDefaultUx.class.getDeclaredConstructors()).hasSize(1); // canonical only
    }
}
