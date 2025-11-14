package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import org.junit.jupiter.api.Test;

public class LHStructPropertyTest {
    @LHStructDef("library")
    class Library {
        public String name;
        public String[] books;
        public int ignoredField;
        public WfRunId maskedField;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String[] getBooks() {
            return this.books;
        }

        public void setBooks(String[] books) {
            this.books = books;
        }

        @LHStructIgnore
        public int getIgnoredField() {
            return this.ignoredField;
        }

        public void setIgnoredField(int val) {
            this.ignoredField = val;
        }

        @LHStructField(masked = true)
        public WfRunId getMaskedField() {
            return this.maskedField;
        }

        public void setMaskedField(WfRunId val) {
            this.maskedField = val;
        }
    }

    @Test
    public void testGetFieldName() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("books", Library.class);
        LHStructProperty lhStructProperty = new LHStructProperty(pd);

        assertThat(lhStructProperty.getFieldName()).isEqualTo("books");
    }

    @Test
    public void testIsIgnored() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("ignoredField", Library.class);
        LHStructProperty lhStructProperty = new LHStructProperty(pd);

        assertThat(lhStructProperty.isIgnored()).isEqualTo(true);
    }

    @Test
    public void testIsMasked() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("maskedField", Library.class);
        LHStructProperty lhStructProperty = new LHStructProperty(pd);

        assertThat(lhStructProperty.isMasked()).isEqualTo(true);
    }

    @Test
    public void testToStructFieldDef() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("books", Library.class);
        LHStructProperty lhStructProperty = new LHStructProperty(pd);

        StructFieldDef actualStructFieldDef = lhStructProperty.toStructFieldDef();
        StructFieldDef expectedStructFieldDef = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder()
                        .setPrimitiveType(VariableType.JSON_ARR)
                        .build())
                .build();

        assertThat(actualStructFieldDef).isEqualTo(expectedStructFieldDef);
    }

    @Test
    public void testGetValueFrom() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("name", Library.class);
        LHStructProperty nameProperty = new LHStructProperty(pd);

        Library library = new Library();
        library.setName("Jedi Archives");

        String expectedPropertyValue = "Jedi Archives";
        String actualPropertyValue = nameProperty.getValueFrom(library).getStr();

        assertThat(expectedPropertyValue).isEqualTo(actualPropertyValue);
    }

    @Test
    public void testSetValueTo() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("name", Library.class);
        LHStructProperty nameProperty = new LHStructProperty(pd);

        Library library = new Library();
        nameProperty.setValueTo(library, LHLibUtil.objToVarVal("Parkway Central"));

        String expectedPropertyValue = "Parkway Central";
        String actualPropertyValue = library.getName();

        assertThat(expectedPropertyValue).isEqualTo(actualPropertyValue);
    }
}
