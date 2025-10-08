package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import lombok.Getter;
import org.junit.jupiter.api.Test;

public class LHStructDefTypeTest {

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

    @LHStructDef(name = "named-field-demo")
    class NamedFieldDemo {
        public int inferredFieldName;

        @LHStructField(name = "customFieldName")
        public int getInferredFieldName() {
            return this.inferredFieldName;
        }
    }

    @Test
    public void getEmptyInlineStructDefWhenClassDoesNotHaveGettersOrSetters() {
        LHStructDefType authorClassType = new LHStructDefType(AuthorFieldsOnly.class);
        InlineStructDef actualInlineStructDef = authorClassType.getInlineStructDef();

        assertThat(actualInlineStructDef.toString()).isBlank();
    }

    @Test
    public void getInlineStructDefWithPrimitiveFields() {
        LHStructDefType authorClassType = new LHStructDefType(Author.class);
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
        InlineStructDef actualInlineStructDef = new LHStructDefType(Book.class).getInlineStructDef();
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
    public void getInlineStructDefIgnoresFieldsWithLHStructIgnore() {
        InlineStructDef actualInlineStructDef = new LHStructDefType(Library.class).getInlineStructDef();
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
                                        .setPrimitiveType(VariableType.JSON_ARR)
                                        .build())
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void getInlineStructDefMarksFieldsAsMasked() {
        InlineStructDef actualInlineStructDef = new LHStructDefType(MaskedValueDemo.class).getInlineStructDef();
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
        InlineStructDef actualInlineStructDef = new LHStructDefType(NamedFieldDemo.class).getInlineStructDef();
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
    public void getStructDefTypeDefinition() {
        LHClassType structDefType = LHClassType.fromJavaClass(Author.class);

        TypeDefinition actualTypeDefinition = structDefType.getTypeDefinition();
        TypeDefinition expectedTypeDefinition = TypeDefinition.newBuilder()
                .setStructDefId(StructDefId.newBuilder().setName("author"))
                .build();

        assertThat(actualTypeDefinition).isEqualTo(expectedTypeDefinition);
    }
}
