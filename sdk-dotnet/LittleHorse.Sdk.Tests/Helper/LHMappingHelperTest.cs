using System;
using System.Collections.Generic;
using Google.Protobuf;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using Google.Protobuf.WellKnownTypes;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Tests;
using LhStruct = LittleHorse.Sdk.Common.Proto.Struct;
using Xunit;
using Type = System.Type;

public class LHMappingHelperTest
{
    [LHStructDef("test-struct")]
    private class TestStruct
    {
        public string? Name { get; set; }
        public int Age { get; set; }
    }

    [LHStructDef("test-struct-ignored")]
    private class TestStructWithIgnored
    {
        public string? Name { get; set; }

        [LHStructIgnore]
        public string? Secret { get; set; }
    }

    private class NonStruct
    {
        public string? Name { get; set; }
        public int Age { get; set; }
    }

    [Fact]
    public void LHHelper_WithSystemIntegralVariableType_ShouldReturnLHVariableIntType()
    {
        var testAllowedTypes = new List<Type>()
        {
            typeof(Int64), typeof(Int32), typeof(Int16), typeof(UInt16), typeof(UInt32), typeof(UInt64), typeof(sbyte),
            typeof(byte), typeof(short), typeof(ushort), typeof(int), typeof(uint), typeof(long), typeof(ulong),
            typeof(nint), typeof(nuint)
        };

        foreach (var type in testAllowedTypes)
        {
            var result = LHMappingHelper.DotNetTypeToLHVariableType(type);

            Assert.True(result == VariableType.Int);
        }
    }

    [Fact]
    public void LHHelper_WithSystemFloatingVariableType_ShouldReturnLHVariableDoubleType()
    {
        var testAllowedTypes = new List<Type>() { typeof(float), typeof(double) };

        foreach (var type in testAllowedTypes)
        {
            var result = LHMappingHelper.DotNetTypeToLHVariableType(type);

            Assert.True(result == VariableType.Double);
        }
    }

    [Fact]
    public void LHHelper_WithSystemStringVariableType_ShouldReturnLHVariableStrType()
    {
        var type = typeof(string);

        var result = LHMappingHelper.DotNetTypeToLHVariableType(type);

        Assert.True(result == VariableType.Str);
    }

    [Fact]
    public void LHHelper_WithSystemBoolVariableType_ShouldReturnLHVariableBoolType()
    {
        var type = typeof(bool);

        var result = LHMappingHelper.DotNetTypeToLHVariableType(type);

        Assert.True(result == VariableType.Bool);
    }

    [Fact]
    public void LHHelper_WithSystemBytesVariableType_ShouldReturnLHVariableBytesType()
    {
        var type = typeof(byte[]);

        var result = LHMappingHelper.DotNetTypeToLHVariableType(type);

        Assert.True(result == VariableType.Bytes);
    }

    [Fact]
    public void LHHelper_WithIlistObjectType_ShouldReturnLHVariableJsonArrType()
    {
        var testAllowedTypes = new List<Type>() { typeof(List<object>), typeof(List<string>), typeof(List<int>) };

        foreach (var type in testAllowedTypes)
        {
            var result = LHMappingHelper.DotNetTypeToLHVariableType(type);

            Assert.True(result == VariableType.JsonArr);
        }
    }

    [Fact]
    public void LHHelper_WithNotAllowedSystemVariableTypes_ShouldReturnLHJsonObj()
    {
        var testNotAllowedTypes = new List<Type>()
        {
            typeof(decimal), typeof(char), typeof(void),
            typeof(Dictionary<string, string>)
        };

        foreach (var type in testNotAllowedTypes)
        {
            var result = LHMappingHelper.DotNetTypeToLHVariableType(type);

            Assert.Equal(VariableType.JsonObj, result);
        }
    }


    [Fact]
    public void LHHelper_WithVariableValue_ShouldReturnSameValue()
    {
        VariableValue value = new VariableValue();
        value.Str = "test";

        var result = LHMappingHelper.ObjectToVariableValue(value);

        Assert.Equal(value, result);
    }

    [Fact]
    public void LHHelper_WithNullLHVariableValue_ShouldReturnNewLHVariableValue()
    {
        var result = LHMappingHelper.ObjectToVariableValue(null);

        Assert.NotNull(result);
    }

    [Fact]
    public void LHHelper_WithIntegerValue_ShouldReturnLHIntegerValue()
    {
        int expectedValue = 23;
        var testIntValues = new List<object>()
        {
            (sbyte)expectedValue, (byte)expectedValue, (short)expectedValue,
            (ushort)expectedValue, expectedValue, (uint)expectedValue, (long)expectedValue, (ulong)expectedValue,
            (nint)expectedValue, (nuint)expectedValue
        };

        foreach (var obj in testIntValues)
        {
            var result = LHMappingHelper.ObjectToVariableValue(obj);

            Assert.Equal(expectedValue, result.Int);
        }
    }

    [Fact]
    public void LHHelper_WithFloatingsValue_ShouldReturnLHDoubleValue()
    {
        var testFloatValues = new List<object>() { 12.3, 3_000.5F, 3D };

        foreach (var value in testFloatValues)
        {
            var result = LHMappingHelper.ObjectToVariableValue(value);

            if (value is double doubleValue)
                Assert.Equal(doubleValue, result.Double);
            if (value is float floatValue)
                Assert.Equal(floatValue, result.Double);
        }
    }

    [Fact]
    public void LHHelper_WithStringValue_ShouldReturnLHStrValue()
    {
        var stringValue = "This is a test";

        var result = LHMappingHelper.ObjectToVariableValue(stringValue);

        Assert.Equal(stringValue, result.Str);
    }
    
    [Fact]
    public void LHHelper_WithDateTimeValue_ShouldReturnLHUtcTimestampValue()
    {
        var currentDateTime = DateTime.Now;

        var result = LHMappingHelper.ObjectToVariableValue(currentDateTime);

        Assert.Equal(currentDateTime.ToUniversalTime().ToTimestamp(), result.UtcTimestamp);
    }
    
    [Fact]
    public void LHHelper_WithWfRunIdValue_ShouldReturnLHWfRunIdValue()
    {
        var wfRunId = new WfRunId()
        {
            Id = "test-id"
        };
        var expectedVariableValue = new VariableValue()
        {
            WfRunId = wfRunId
        };

        var result = LHMappingHelper.ObjectToVariableValue(wfRunId);

        Assert.Equal(result, expectedVariableValue);
    }

    [Fact]
    public void LHHelper_WithBoolValue_ShouldReturnLHBoolValue()
    {
        var boolValue = true;

        var result = LHMappingHelper.ObjectToVariableValue(boolValue);

        Assert.Equal(boolValue, result.Bool);
    }

    [Fact]
    public void LHHelper_WithBytesValue_ShouldReturnLHBytesValue()
    {
        var bytes = new byte[] { 0x20, 0x20, 0x20 };

        var result = LHMappingHelper.ObjectToVariableValue(bytes);

        Assert.Equal(bytes, result.Bytes);
    }

    [Fact]
    public void LHHelper_WithListOfObjectsValue_ShouldReturnLHJsonArrValue()
    {
        var car = new Car { Id = 1, Cost = 134.45E-2f };
        var persons = new List<Person>()
        {
            new Person() { Age = 36, Cars = new List<Car>() { car }, FirstName = "Test1" },
            new Person() { Age = 32, Cars = new List<Car>() { car }, FirstName = "Test2" }
        };

        var result = LHMappingHelper.ObjectToVariableValue(persons);

        Assert.Contains("\"Age\":36", result.JsonArr);
        Assert.Contains("\"FirstName\":\"Test2\"", result.JsonArr);
    }

    [Fact]
    public void LHHelper_WithCustomObjectValue_ShouldReturnLHJsonObjValue()
    {
        var car = new Car { Id = 1, Cost = 134.45E-2f };
        var person = new Person() { Age = 36, Cars = new List<Car>() { car }, FirstName = "Test" };

        var result = LHMappingHelper.ObjectToVariableValue(person);

        Assert.Contains("\"FirstName\":\"Test\"", result.JsonObj);
    }

    [Fact]
    public void LHHelper_WithLHVariableValueType_ShouldReturnLHVariableType()
    {
        var intVariableValue = new VariableValue { Int = 1234 };
        var doubleVariableValue = new VariableValue { Double = 12.35 };
        var stringVariableValue = new VariableValue { Str = "test" };
        var boolVariableValue = new VariableValue { Bool = true };
        var timestampVariableValue = new VariableValue { UtcTimestamp = DateTime.UtcNow.ToTimestamp() };
        var wfRunIdVariableValue = new VariableValue { WfRunId = new WfRunId() {Id = "test"} };
        var bytesVariableValue = new VariableValue { Bytes = ByteString.FromBase64("aG9sYQ==") };
        var jsonArrayVariableValue = new VariableValue { JsonArr = "[{\"name\": \"obiwan\"}, {\"name\": \"pepito\"}]" };

        var variableValues = new Dictionary<VariableType, VariableValue>
        {
            { VariableType.Int, intVariableValue },
            { VariableType.Double, doubleVariableValue },
            { VariableType.Str, stringVariableValue },
            { VariableType.Bool, boolVariableValue },
            { VariableType.Bytes, bytesVariableValue },
            { VariableType.JsonArr, jsonArrayVariableValue },
            { VariableType.Timestamp, timestampVariableValue },
            { VariableType.WfRunId, wfRunIdVariableValue }
        };


        foreach (var variableValue in variableValues)
        {
            var expectedType = variableValue.Key;
            var result = LHMappingHelper.ValueCaseToVariableType(variableValue.Value.ValueCase);

            Assert.Equal(expectedType, result);
        }
    }

    [Fact]
    public void LHHelper_WithLHVariableValueType_ShouldReturnLHJsonVariableType()
    {
        var jsonObjVariableValue = new VariableValue { JsonObj = "{\"name\": \"obiwan\"}" };
        var noneVariableValue = new VariableValue();

        var variableValues = new List<VariableValue> { jsonObjVariableValue, noneVariableValue };

        foreach (var variableValue in variableValues)
        {
            var result = LHMappingHelper.ValueCaseToVariableType(variableValue.ValueCase);

            Assert.Equal(VariableType.JsonObj, result);
        }
    }

    [Fact]
    public void DotNetTypeToReturnType_WithIntType_ShouldReturnCorrectReturnType()
    {
        var result = LHMappingHelper.DotNetTypeToReturnType(typeof(int));

        Assert.NotNull(result);
        Assert.NotNull(result.ReturnType_);
        Assert.Equal(VariableType.Int, result.ReturnType_.PrimitiveType);
    }

    [Fact]
    public void DotNetTypeToReturnType_WithStructDefType_ShouldReturnStructTypeDefinition()
    {
        var result = LHMappingHelper.DotNetTypeToReturnType(typeof(TestStruct));

        Assert.NotNull(result);
        Assert.NotNull(result.ReturnType_);
        Assert.Equal(TypeDefinition.DefinedTypeOneofCase.StructDefId, result.ReturnType_.DefinedTypeCase);
        Assert.Equal("test-struct", result.ReturnType_.StructDefId.Name);
    }

    [Fact]
    public void DotNetTypeToReturnType_WithNull_ShouldThrowException()
    {
        var exception = Assert.Throws<ArgumentNullException>(() => LHMappingHelper.DotNetTypeToReturnType(null!));

        Assert.Equal("type", exception.ParamName);
        Assert.Equal("Type cannot be null. (Parameter 'type')", exception.Message);
    }

    [Fact]
    public void ObjectToVariableValue_WithStructDefObject_ShouldSerializeStruct()
    {
        var obj = new TestStruct { Name = "Ada", Age = 42 };

        var result = LHMappingHelper.ObjectToVariableValue(obj);

        Assert.Equal(VariableValue.ValueOneofCase.Struct, result.ValueCase);
        Assert.Equal("test-struct", result.Struct.StructDefId.Name);
        Assert.True(result.Struct.Struct_.Fields.ContainsKey("name"));
        Assert.True(result.Struct.Struct_.Fields.ContainsKey("age"));
        Assert.Equal("Ada", result.Struct.Struct_.Fields["name"].Value.Str);
        Assert.Equal(42, result.Struct.Struct_.Fields["age"].Value.Int);
    }

    [Fact]
    public void ObjectToVariableValue_WithStructDefObject_ShouldSkipIgnoredFields()
    {
        var obj = new TestStructWithIgnored { Name = "Leia", Secret = "hidden" };

        var result = LHMappingHelper.ObjectToVariableValue(obj);

        Assert.Equal(VariableValue.ValueOneofCase.Struct, result.ValueCase);
        Assert.Equal("test-struct-ignored", result.Struct.StructDefId.Name);
        Assert.True(result.Struct.Struct_.Fields.ContainsKey("name"));
        Assert.False(result.Struct.Struct_.Fields.ContainsKey("secret"));
    }

    [Fact]
    public void VariableValueToObject_WithStruct_ShouldDeserializeToObject()
    {
        var inlineStruct = new InlineStruct();
        inlineStruct.Fields.Add("name", new StructField { Value = new VariableValue { Str = "Ada" } });
        inlineStruct.Fields.Add("age", new StructField { Value = new VariableValue { Int = 42 } });

        var structValue = new LhStruct
        {
            Struct_ = inlineStruct
        };

        var variableValue = new VariableValue { Struct = structValue };

        var result = (TestStruct)LHMappingHelper.VariableValueToObject(variableValue, typeof(TestStruct))!;

        Assert.Equal("Ada", result.Name);
        Assert.Equal(42, result.Age);
    }

    [Fact]
    public void VariableValueToObject_WithStructAndNonStructType_ShouldThrow()
    {
        var inlineStruct = new InlineStruct();
        inlineStruct.Fields.Add("name", new StructField { Value = new VariableValue { Str = "Ada" } });
        inlineStruct.Fields.Add("age", new StructField { Value = new VariableValue { Int = 42 } });

        var structValue = new LhStruct
        {
            Struct_ = inlineStruct
        };

        var variableValue = new VariableValue { Struct = structValue };

        Assert.Throws<LHSerdeException>(() => LHMappingHelper.VariableValueToObject(variableValue, typeof(NonStruct)));
    }

    [Fact]
    public void VariableValueToObject_WithStructMissingField_ShouldThrow()
    {
        var inlineStruct = new InlineStruct();
        inlineStruct.Fields.Add("Name", new StructField { Value = new VariableValue { Str = "Ada" } });

        var structValue = new LhStruct
        {
            Struct_ = inlineStruct
        };

        var variableValue = new VariableValue { Struct = structValue };

        Assert.Throws<LHSerdeException>(() => LHMappingHelper.VariableValueToObject(variableValue, typeof(TestStruct)));
    }
}