package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import org.junit.jupiter.api.Test;

public class LHStructPropertyTest {

    @Test
    public void testGetFieldName() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("books", Library.class);
        LHStructProperty lhStructProperty = new LHStructProperty(pd, new LHStructDefType(Library.class));

        assertThat(lhStructProperty.getFieldName()).isEqualTo("books");
    }

    @Test
    public void testIsIgnored() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("ignoredField", Library.class);
        LHStructProperty lhStructProperty = new LHStructProperty(pd, new LHStructDefType(Library.class));

        assertThat(lhStructProperty.isIgnored()).isEqualTo(true);
    }

    @Test
    public void testIsMasked() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("maskedField", Library.class);
        LHStructProperty lhStructProperty = new LHStructProperty(pd, new LHStructDefType(Library.class));

        assertThat(lhStructProperty.isMasked()).isEqualTo(true);
    }

    @Test
    public void testToStructFieldDef() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("books", Library.class);
        LHStructProperty lhStructProperty = new LHStructProperty(pd, new LHStructDefType(Library.class));

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
        LHStructProperty nameProperty = new LHStructProperty(pd, new LHStructDefType(Library.class));

        Library library = new Library();
        library.setName("Jedi Archives");

        String expectedPropertyValue = "Jedi Archives";
        String actualPropertyValue = nameProperty.getValueFrom(library).getStr();

        assertThat(expectedPropertyValue).isEqualTo(actualPropertyValue);
    }

    @Test
    public void testSetValueTo() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("name", Library.class);
        LHStructProperty nameProperty = new LHStructProperty(pd, new LHStructDefType(Library.class));

        Library library = new Library();
        nameProperty.setValueTo(library, LHLibUtil.objToVarVal("Parkway Central"));

        String expectedPropertyValue = "Parkway Central";
        String actualPropertyValue = library.getName();

        assertThat(expectedPropertyValue).isEqualTo(actualPropertyValue);
    }

    @Test
    public void testHasDefaultValue() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("stringWithDefault", Library.class);
        LHStructProperty stringWithDefaultProperty = new LHStructProperty(pd, new LHStructDefType(Library.class));

        VariableValue expectedPropertyValue =
                VariableValue.newBuilder().setStr("hello").build();
        VariableValue actualPropertyValue =
                stringWithDefaultProperty.getDefaultValue().get();

        assertThat(expectedPropertyValue).isEqualTo(actualPropertyValue);
    }
}
