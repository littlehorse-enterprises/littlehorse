using System;
using System.Collections.Generic;
using System.ComponentModel;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Worker;
using Xunit;

public class LHStructPropertyTest
{
    [LHStructDef("library")]
    private class Library
    {
        public List<string> Books { get; set; } = new();

        public string? Name { get; set; }

        [LHStructIgnore]
        public string? IgnoredField { get; set; }

        [LHStructField(masked: true)]
        public string? MaskedField { get; set; }

        [LHStructField(name: "custom_name")]
        public string? CustomNamed { get; set; }

        public string StringWithDefault { get; set; } = "hello";

        public string ReadOnly => "readonly";

        public string WriteOnly { private get; set; } = "";
    }

    private static LHStructProperty CreateProperty(string name)
    {
        var pd = TypeDescriptor.GetProperties(typeof(Library)).Find(name, false);
        Assert.NotNull(pd);
        return new LHStructProperty(pd!, new LHStructDefType(typeof(Library)));
    }

    [Fact]
    public void GetFieldName_UsesPropertyName_WhenNoAttribute()
    {
        var property = CreateProperty(nameof(Library.Books));

        Assert.Equal("books", property.FieldName);
    }

    [Fact]
    public void GetFieldName_UsesAttributeName_WhenProvided()
    {
        var property = CreateProperty(nameof(Library.CustomNamed));

        Assert.Equal("custom_name", property.FieldName);
    }

    [Fact]
    public void IsIgnored_True_WhenAttributePresent()
    {
        var property = CreateProperty(nameof(Library.IgnoredField));

        Assert.True(property.Ignored);
    }

    [Fact]
    public void IsMasked_True_WhenAttributePresent()
    {
        var property = CreateProperty(nameof(Library.MaskedField));

        Assert.True(property.Masked);
    }

    [Fact]
    public void ToStructFieldDef_MapsPrimitiveTypeAndMask()
    {
        var property = CreateProperty(nameof(Library.MaskedField));

        StructFieldDef fieldDef = property.ToStructFieldDef();

        Assert.Equal(VariableType.Str, fieldDef.FieldType.PrimitiveType);
        Assert.True(fieldDef.FieldType.Masked);
    }

    [Fact]
    public void ToStructFieldDef_MapsListToJsonArray()
    {
        var property = CreateProperty(nameof(Library.Books));

        StructFieldDef fieldDef = property.ToStructFieldDef();

        Assert.Equal(VariableType.JsonArr, fieldDef.FieldType.PrimitiveType);
    }

    [Fact]
    public void GetValueFrom_ReturnsVariableValue()
    {
        var property = CreateProperty(nameof(Library.Name));
        var library = new Library { Name = "Jedi Archives" };

        var value = property.GetValueFrom(library);

        Assert.NotNull(value);
        Assert.Equal("Jedi Archives", value!.Str);
    }

    [Fact]
    public void SetValueTo_SetsPropertyFromVariableValue()
    {
        var property = CreateProperty(nameof(Library.Name));
        var library = new Library();

        property.SetValueTo(library, new VariableValue { Str = "Parkway Central" });

        Assert.Equal("Parkway Central", library.Name);
    }

    [Fact]
    public void GetDefaultValue_ReturnsDefaultValue_WhenPresent()
    {
        var property = CreateProperty(nameof(Library.StringWithDefault));

        var value = property.GetDefaultValue();

        Assert.NotNull(value);
        Assert.Equal("hello", value!.Str);
    }

    [Fact]
    public void SetValueTo_Throws_WhenNoSetter()
    {
        var property = CreateProperty(nameof(Library.ReadOnly));
        var library = new Library();

        Assert.Throws<InvalidOperationException>(() => property.SetValueTo(library, new VariableValue { Str = "x" }));
    }

    [Fact]
    public void GetValueFrom_ReturnsNull_WhenPropertyValueNull()
    {
        var property = CreateProperty(nameof(Library.Name));
        var library = new Library { Name = null };

        var value = property.GetValueFrom(library);

        Assert.Null(value);
    }

    [Fact]
    public void SetValueTo_ThrowsSerdeException_OnTypeMismatch()
    {
        var property = CreateProperty(nameof(Library.Name));
        var library = new Library();

        Assert.Throws<LHSerdeException>(() => property.SetValueTo(library, new VariableValue { Int = 12 }));
    }

    [Fact]
    public void GetStructProperties_ExcludesIgnoredProperties()
    {
        var structDef = new LHStructDefType(typeof(Library));

        var properties = structDef.GetStructProperties();

        Assert.DoesNotContain(properties, p => p.FieldName == nameof(Library.IgnoredField));
    }
}
