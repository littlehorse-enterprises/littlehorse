package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.InlineMapDef;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHStructDef;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class LHStructPropertyTest {

    @LHStructDef("property-address")
    public record PropertyAddress(String street) {}

    @LHStructDef("record-map-property")
    public record RecordMapProperty(Map<String, PropertyAddress> addresses) {}

    @LHStructDef("pojo-map-property")
    public static class PojoMapProperty {
        private Map<String, PropertyAddress> addresses;

        public Map<String, PropertyAddress> getAddresses() {
            return addresses;
        }

        public void setAddresses(Map<String, PropertyAddress> addresses) {
            this.addresses = addresses;
        }
    }

    @LHStructDef("${company}-property-address")
    public record PlaceholderPropertyAddress(String street) {}

    @LHStructDef("${company}-record-property")
    public record PlaceholderRecordProperty(PlaceholderPropertyAddress address) {}

    @Test
    public void testGetFieldName() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("books", Library.class);
        LHStructProperty lhStructProperty =
                new LHStructProperty(pd, new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty()));

        assertThat(lhStructProperty.getFieldName()).isEqualTo("books");
    }

    @Test
    public void testIsIgnored() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("ignoredField", Library.class);
        LHStructProperty lhStructProperty =
                new LHStructProperty(pd, new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty()));

        assertThat(lhStructProperty.isIgnored()).isEqualTo(true);
    }

    @Test
    public void testIsMasked() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("maskedField", Library.class);
        LHStructProperty lhStructProperty =
                new LHStructProperty(pd, new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty()));

        assertThat(lhStructProperty.isMasked()).isEqualTo(true);
    }

    @Test
    public void testToStructFieldDef() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("books", Library.class);
        LHStructProperty lhStructProperty =
                new LHStructProperty(pd, new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty()));

        StructFieldDef actualStructFieldDef = lhStructProperty.toStructFieldDef();
        StructFieldDef expectedStructFieldDef = StructFieldDef.newBuilder()
                .setFieldType(TypeDefinition.newBuilder()
                        .setInlineArrayDef(InlineArrayDef.newBuilder()
                                .setArrayType(TypeDefinition.newBuilder()
                                        .setPrimitiveType(VariableType.STR)
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(actualStructFieldDef).isEqualTo(expectedStructFieldDef);
    }

    @Test
    public void testGetValueFrom() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("name", Library.class);
        LHStructProperty nameProperty =
                new LHStructProperty(pd, new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty()));

        Library library = new Library();
        library.setName("Jedi Archives");

        String expectedPropertyValue = "Jedi Archives";
        String actualPropertyValue = nameProperty.getValueFrom(library).getStr();

        assertThat(expectedPropertyValue).isEqualTo(actualPropertyValue);
    }

    @Test
    public void testSetValueTo() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("name", Library.class);
        LHStructProperty nameProperty =
                new LHStructProperty(pd, new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty()));

        Library library = new Library();
        nameProperty.setValueTo(library, LHLibUtil.objToVarVal("Parkway Central"));

        String expectedPropertyValue = "Parkway Central";
        String actualPropertyValue = library.getName();

        assertThat(expectedPropertyValue).isEqualTo(actualPropertyValue);
    }

    @Test
    public void testHasDefaultValue() throws IntrospectionException {
        PropertyDescriptor pd = new PropertyDescriptor("stringWithDefault", Library.class);
        LHStructProperty stringWithDefaultProperty =
                new LHStructProperty(pd, new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty()));

        VariableValue expectedPropertyValue =
                VariableValue.newBuilder().setStr("hello").build();
        VariableValue actualPropertyValue =
                stringWithDefaultProperty.getDefaultValue().get();

        assertThat(expectedPropertyValue).isEqualTo(actualPropertyValue);
    }

    @Test
    public void structProperty_withLHArrayAnnotation_emitsInlineArrayDef() throws Exception {

        LHStructDefType parent = new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty());
        PropertyDescriptor pd = new PropertyDescriptor("lhArrayWithDefault", Library.class);
        LHStructProperty prop = new LHStructProperty(pd, parent);

        StructFieldDef fieldDef = prop.toStructFieldDef(LHTypeAdapterRegistry.empty());
        TypeDefinition typeDef = fieldDef.getFieldType();

        assertThat(typeDef.getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_ARRAY_DEF);

        InlineArrayDef arr = typeDef.getInlineArrayDef();
        assertThat(arr).isNotNull();
        assertThat(arr.getArrayType().getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.PRIMITIVE_TYPE);
        assertThat(arr.getArrayType().getPrimitiveType()).isEqualTo(VariableType.STR);
    }

    @Test
    public void getDefaultValue_returnsNativeArray() throws Exception {
        LHStructDefType parent = new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty());
        PropertyDescriptor pd = new PropertyDescriptor("lhArrayWithDefault", Library.class);
        LHStructProperty prop = new LHStructProperty(pd, parent);

        Optional<VariableValue> maybe = prop.getDefaultValue(LHTypeAdapterRegistry.empty());
        VariableValue def = maybe.get();

        assertThat(def.getValueCase()).isEqualTo(VariableValue.ValueCase.ARRAY);
        assertThat(def.getArray().getItemsCount()).isEqualTo(2);
        assertThat(def.getArray().getItems(0).getStr()).isEqualTo("a");
    }

    @Test
    public void structProperty_withMapType_emitsInlineMapDef() throws Exception {
        LHStructDefType parent = new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty());
        PropertyDescriptor pd = new PropertyDescriptor("inventory", Library.class);
        LHStructProperty prop = new LHStructProperty(pd, parent);

        StructFieldDef fieldDef = prop.toStructFieldDef(LHTypeAdapterRegistry.empty());
        TypeDefinition typeDef = fieldDef.getFieldType();

        assertThat(typeDef.getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.INLINE_MAP_DEF);

        InlineMapDef mapDef = typeDef.getInlineMapDef();
        assertThat(mapDef.getKeyType().getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.PRIMITIVE_TYPE);
        assertThat(mapDef.getKeyType().getPrimitiveType()).isEqualTo(VariableType.STR);
        assertThat(mapDef.getValueType().getDefinedTypeCase()).isEqualTo(TypeDefinition.DefinedTypeCase.PRIMITIVE_TYPE);
        assertThat(mapDef.getValueType().getPrimitiveType()).isEqualTo(VariableType.INT);
    }

    @Test
    public void structProperty_withMapValue_serializesAsNativeMap() throws Exception {
        LHStructDefType parent = new LHStructDefType(Library.class, LHTypeAdapterRegistry.empty());
        PropertyDescriptor pd = new PropertyDescriptor("inventory", Library.class);
        LHStructProperty prop = new LHStructProperty(pd, parent);

        Library library = new Library();
        library.setInventory(java.util.Map.of("sci-fi", 42L, "fantasy", 17L));

        VariableValue val = prop.getValueFrom(library, LHTypeAdapterRegistry.empty());

        assertThat(val.getValueCase()).isEqualTo(VariableValue.ValueCase.MAP);
        assertThat(val.getMap().getEntriesCount()).isEqualTo(2);
    }

    @Test
    void deserializeValue_returnsTypedNativeMapForRecordComponent() throws Exception {
        LHStructDefType parent = new LHStructDefType(RecordMapProperty.class, LHTypeAdapterRegistry.empty());
        LHStructProperty property = parent.getStructProperties().get(0);
        RecordMapProperty original = new RecordMapProperty(Map.of("home", new PropertyAddress("Main St")));

        VariableValue serialized = property.getValueFrom(original, LHTypeAdapterRegistry.empty());
        Object deserialized = property.deserializeValue(serialized, LHTypeAdapterRegistry.empty(), Map.of());

        assertThat(deserialized).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) deserialized).get("home")).isEqualTo(new PropertyAddress("Main St"));
    }

    @Test
    void setValueTo_usesTypedNativeMapDeserialization() throws Exception {
        LHStructDefType parent = new LHStructDefType(PojoMapProperty.class, LHTypeAdapterRegistry.empty());
        LHStructProperty property = parent.getStructProperties().get(0);
        PojoMapProperty source = new PojoMapProperty();
        source.setAddresses(Map.of("home", new PropertyAddress("Main St")));
        VariableValue serialized = property.getValueFrom(source, LHTypeAdapterRegistry.empty());
        PojoMapProperty target = new PojoMapProperty();

        property.setValueTo(target, serialized, LHTypeAdapterRegistry.empty(), Map.of());

        assertThat(target.getAddresses()).containsEntry("home", new PropertyAddress("Main St"));
    }

    @Test
    void deserializeValue_propagatesPlaceholdersToNestedRecord() throws Exception {
        Map<String, String> placeholders = Map.of("company", "acme");
        LHStructDefType parent =
                new LHStructDefType(PlaceholderRecordProperty.class, LHTypeAdapterRegistry.empty(), placeholders);
        LHStructProperty property = parent.getStructProperties().get(0);
        PlaceholderRecordProperty original = new PlaceholderRecordProperty(new PlaceholderPropertyAddress("Main St"));

        VariableValue serialized = property.getValueFrom(original, LHTypeAdapterRegistry.empty(), placeholders);
        Object deserialized = property.deserializeValue(serialized, LHTypeAdapterRegistry.empty(), placeholders);

        assertThat(deserialized).isEqualTo(new PlaceholderPropertyAddress("Main St"));
    }
}
