package io.littlehorse.sdk.wfsdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.littlehorse.sdk.common.exception.StructDefCircularDependencyException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.StructDefUtil;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class StructDefUtilTest {

    @LHStructDef(name = "book")
    class Book {
        public String title;
        public int numPages;
        public Author author;
    }

    @LHStructDef(name = "author")
    class Author {
        public String name;
        public int age;
        public boolean isAlive;
        public double height;
        public byte[] bytes;
        public WfRunId wfRunId;
    }

    class Library {
        public String name;

        @LHStructIgnore
        public Book[] books;
    }

    class MaskedValueDemo {
        @LHStructField(masked = true)
        public String maskedValue;
    }

    class NamedFieldDemo {
        @LHStructField(name = "setFieldName")
        public int inferredFieldName;
    }

    @Test
    public void buildInlineStructDefWithPrimitiveFields() {
        InlineStructDef actualInlineStructDef = StructDefUtil.buildInlineStructDef(Author.class);
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
                        "isAlive",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.BOOL))
                                .build())
                .putFields(
                        "height",
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
        InlineStructDef actualInlineStructDef = StructDefUtil.buildInlineStructDef(Book.class);
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
        InlineStructDef actualInlineStructDef = StructDefUtil.buildInlineStructDef(Library.class);
        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "name",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.STR))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    @Test
    public void buildInlineStructDefMarksFieldsAsMasked() {
        InlineStructDef actualInlineStructDef = StructDefUtil.buildInlineStructDef(MaskedValueDemo.class);
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
        InlineStructDef actualInlineStructDef = StructDefUtil.buildInlineStructDef(NamedFieldDemo.class);
        InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
                .putFields(
                        "setFieldName",
                        StructFieldDef.newBuilder()
                                .setFieldType(TypeDefinition.newBuilder().setPrimitiveType(VariableType.INT))
                                .build())
                .build();

        assertThat(actualInlineStructDef).isEqualTo(expectedInlineStructDef);
    }

    // TODO: Support Arrays again
    // public void buildInlineStructDefWithFieldArrayOfStructDefs() {
    //     InlineStructDef actualInlineStructDef = StructDefUtil.buildInlineStructDef(Library.class);
    //     InlineStructDef expectedInlineStructDef = InlineStructDef.newBuilder()
    //             .putFields(
    //                     "books",
    //                     StructFieldDef.newBuilder()
    //                             .setFieldType(TypeDefinition.newBuilder().setInlineStructDef())
    //                             .build())
    //             .build();
    // }

    @Nested
    class NoCycleTest {
        @Test
        public void getStructDefDependenciesCompletesWithoutExceptionWhenNoCircularDependencies() {
            StructDefUtil.getStructDefDependencies(Car.class);
        }

        @Test
        public void getStructDefDependenciesReturnsTopologicallySortedListOfDependencies() {
            List<Class<?>> classList = StructDefUtil.getStructDefDependencies(Car.class);
            List<Class<?>> expectedClassList = List.of(CarID.class, Garage.class, Person.class, Car.class);

            assertThat(expectedClassList).isEqualTo(classList);
        }

        @LHStructDef(name = "car")
        class Car {
            public Garage garage;
            public CarID id;
            public Person owner;
            public Person passenger;
        }

        @LHStructDef(name = "person")
        class Person {
            public Garage garage;
        }

        @LHStructDef(name = "garage")
        class Garage {
            public CarID[] carIDs;
        }

        @LHStructDef(name = "carId")
        class CarID {
            public String uuid;
        }
    }

    @Nested
    class CycleTest {
        @Test
        public void getStructDefDependenciesThrowsExceptionWhenCircularDependencies() {
            assertThatThrownBy(() -> {
                        StructDefUtil.getStructDefDependencies(Car.class);
                    })
                    .isInstanceOf(StructDefCircularDependencyException.class);
        }

        @LHStructDef(name = "car")
        class Car {
            public Person owner;
            public Person passenger;
        }

        @LHStructDef(name = "person")
        class Person {
            public Garage garage;
        }

        @LHStructDef(name = "garage")
        class Garage {
            public Car[] cars;
        }
    }
}
