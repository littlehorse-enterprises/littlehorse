package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;

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
import lombok.Getter;
import org.junit.jupiter.api.Test;

public class LHClassTypeTest {
    @LHStructDef(name = "book")
    @Getter
    class Book {
        public String title;
        public int numPages;
        public Author author;
    }

    @LHStructDef(name = "authorFieldsOnly")
    class AuthorFieldsOnly {
        public String name;
        public int age;
        public boolean isAlive;
        public double heightCm;
        public byte[] bytes;
        public WfRunId wfRunId;
    }

    @LHStructDef(name = "author")
    @Getter
    class Author {
        public String name;
        public int age;
        public boolean isAlive;
        public double heightCm;
        public byte[] bytes;
        public WfRunId wfRunId;
    }

    @LHStructDef(name = "library")
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
    @LHStructDef(name = "maskedValueDemo")
    class MaskedValueDemo {
        public String maskedValue;

        @LHStructField(masked = true)
        public String getMaskedValue() {
            return this.maskedValue;
        }
    }

    class NamedFieldDemo {
        public int inferredFieldName;

        @LHStructField(name = "customFieldName")
        public int getInferredFieldName() {
            return this.inferredFieldName;
        }
    }

    @Test
    public void buildEmptyInlineStructDefWhenClassDoesNotHaveGettersOrSetters() {
        LHClassType authorClassType = new LHClassType(AuthorFieldsOnly.class);
        InlineStructDef actualInlineStructDef = authorClassType.getInlineStructDef();

        assertThat(actualInlineStructDef.toString()).isBlank();
    }

    @Test
    public void buildInlineStructDefWithPrimitiveFields() {
        LHClassType authorClassType = new LHClassType(Author.class);
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
    public void buildInlineStructDefWithFieldReferenceToAnotherStructDef() {
        InlineStructDef actualInlineStructDef = new LHClassType(Book.class).getInlineStructDef();
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
                                        .setStructDefId(StructDefId.newBuilder().setName("author")))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void buildInlineStructDefIgnoresFieldsWithLHStructIgnore() {
        InlineStructDef actualInlineStructDef = new LHClassType(Library.class).getInlineStructDef();
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
                                                .setElementType(TypeDefinition.newBuilder()
                                                        .setStructDefId(StructDefId.newBuilder()
                                                                .setName("book")))))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void buildInlineStructDefMarksFieldsAsMasked() {
        InlineStructDef actualInlineStructDef = new LHClassType(MaskedValueDemo.class).getInlineStructDef();
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
    public void buildInlineStructDefUsesAnnotatedFieldName() {
        InlineStructDef actualInlineStructDef = new LHClassType(NamedFieldDemo.class).getInlineStructDef();
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
    public void getLHPrimitiveTypeDefinition() {
        LHClassType intType = new LHClassType(Integer.class);

        TypeDefinition actualTypeDefinition = intType.getTypeDefinition();
        TypeDefinition expectedTypeDefinition =
                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT).build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    public void getStructDefTypeDefinition() {
        LHClassType structDefType = new LHClassType(Author.class);

        TypeDefinition actualTypeDefinition = structDefType.getTypeDefinition();
        TypeDefinition expectedTypeDefinition = TypeDefinition.newBuilder()
                .setStructDefId(StructDefId.newBuilder().setName("author"))
                .build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    public void getArrayOfPrimitiveTypeDefinition() {
        LHClassType intArrayTypeDef = new LHClassType(Integer[].class);

        TypeDefinition actualTypeDefinition = intArrayTypeDef.getTypeDefinition();
        TypeDefinition expectedTypeDefinition = TypeDefinition.newBuilder()
                .setInlineArrayDef(InlineArrayDef.newBuilder()
                        .setElementType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT)))
                .build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }

    @Test
    public void get2DArrayOfPrimitiveTypeDefinition() {
        LHClassType intArrayTypeDef = new LHClassType(Integer[][].class);

        TypeDefinition actualTypeDefinition = intArrayTypeDef.getTypeDefinition();
        TypeDefinition expectedTypeDefinition = TypeDefinition.newBuilder()
                .setInlineArrayDef(InlineArrayDef.newBuilder()
                        .setElementType(TypeDefinition.newBuilder()
                                .setInlineArrayDef(InlineArrayDef.newBuilder()
                                        .setElementType(
                                                TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT)))))
                .build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }
}
