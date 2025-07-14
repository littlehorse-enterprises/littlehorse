using System;
using System.Collections.Generic;
using Google.Protobuf;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using Google.Protobuf.WellKnownTypes;
using LittleHorse.Sdk.Tests;
using Xunit;
using Type = System.Type;

public class LHMappingHelperTest
{
    [Fact]
    public void LHHelper_WithSystemIntegralVariableType_ShouldReturnLHVariableIntType()
    {
        var testAllowedTypes = new List<Type>() { typeof(Int64), typeof(Int32), typeof(Int16) 
            , typeof(UInt16), typeof(UInt32), typeof(UInt64)
            , typeof(sbyte), typeof(byte), typeof(short), typeof(ushort)
            , typeof(int), typeof(uint), typeof(long) 
            , typeof(ulong), typeof(nint), typeof(nuint)};
        
        foreach (var type in testAllowedTypes)
        {
            var result = LHMappingHelper.DotNetTypeToLHVariableType(type);
            
            Assert.True(result == VariableType.Int);
        }
    }
    
    [Fact]
    public void LHHelper_WithSystemFloatingVariableType_ShouldReturnLHVariableDoubleType()
    {
        var testAllowedTypes = new List<Type>() { typeof(float), typeof(double)};
        
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
        var testAllowedTypes = new List<Type>() { typeof(List<object>), typeof(List<string>), typeof(List<int>)};
        
        foreach (var type in testAllowedTypes)
        {
            var result = LHMappingHelper.DotNetTypeToLHVariableType(type);
            
            Assert.True(result == VariableType.JsonArr);
        }
    }
    
    [Fact]
    public void LHHelper_WithNotAllowedSystemVariableTypes_ShouldReturnLHJsonObj()
    {
        var testNotAllowedTypes = new List<Type>() { typeof(decimal), typeof(char), typeof(void), 
            typeof(Dictionary<string, string>) };
        
        foreach (var type in testNotAllowedTypes)
        {
            var result = LHMappingHelper.DotNetTypeToLHVariableType(type);
            
            Assert.Equal(VariableType.JsonObj, result);
        }
    }

    [Fact]
    public void LHHelper_WithNullProtoTimestamp_ShouldReturnNull()
    {
        var result = LHMappingHelper.DateTimeFromProtoTimeStamp(null!);
        
        Assert.Null(result);
    }
    
    [Fact]
    public void LHHelper_WithoutDateTicks_ShouldReturnCurrentDateTime()
    {
        Timestamp protoTimestamp = new Timestamp
        {
            Seconds = 0,
            Nanos = 0
        };
        
        var result = LHMappingHelper.DateTimeFromProtoTimeStamp(protoTimestamp);
        
        DateTime now = DateTime.Now;
        DateTime expectedDatetimeWithoutSeconds = new DateTime(now.Year, now.Month, now.Day, now.Hour, now.Minute, 0);
        DateTime actualDatetimeWithoutSeconds = new DateTime(result!.Value.Year, result.Value.Month, result.Value.Day, result.Value.Hour, result.Value.Minute, 0);
        Assert.Equal(expectedDatetimeWithoutSeconds, actualDatetimeWithoutSeconds);
    }
    
    [Fact]
    public void LHHelper_WithSpecificProtoTimestamp_ShouldReturnSpecificDateTime()
    {
        DateTime specificDateTime = new DateTime(2024, 08, 16, 13, 0, 0, DateTimeKind.Utc);
        
        Timestamp specificTimestamp = Timestamp.FromDateTime(specificDateTime);
        
        var result = LHMappingHelper.DateTimeFromProtoTimeStamp(specificTimestamp);
        
        Assert.Equal(specificDateTime, result);
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
        var testIntValues = new List<object>() { (sbyte)expectedValue, (byte)expectedValue, (short)expectedValue, 
            (ushort)expectedValue, expectedValue, (uint)expectedValue, (long)expectedValue, (ulong)expectedValue, 
            (nint)expectedValue, (nuint)expectedValue };
        
        foreach (var obj in testIntValues)
        {
            var result = LHMappingHelper.ObjectToVariableValue(obj);
            
            Assert.Equal(expectedValue, result.Int);
        }
    }

    [Fact]
    public void LHHelper_WithFloatingsValue_ShouldReturnLHDoubleValue()
    {
       var testFloatValues = new List<object>() { 12.3, 3_000.5F, 3D};

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
        var car = new Car {Id = 1, Cost = 134.45E-2f};
        var persons = new List<Person>()
        {
            new Person(){ Age = 36, Cars = new List<Car>() {car}, FirstName = "Test1"},
            new Person(){ Age = 32, Cars = new List<Car>() {car}, FirstName = "Test2"}
        };
        
        var result = LHMappingHelper.ObjectToVariableValue(persons);
        
        Assert.Contains("\"Age\":36", result.JsonArr);
        Assert.Contains("\"FirstName\":\"Test2\"", result.JsonArr);
    }
    
    [Fact]
    public void LHHelper_WithCustomObjectValue_ShouldReturnLHJsonObjValue()
    {
        var car = new Car {Id = 1, Cost = 134.45E-2f};
        var person = new Person() { Age = 36, Cars = new List<Car>() {car}, FirstName = "Test"};
        
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
        var bytesVariableValue = new VariableValue { Bytes = ByteString.FromBase64("aG9sYQ==") };
        var jsonArrayVariableValue = new VariableValue { JsonArr = "[{\"name\": \"obiwan\"}, {\"name\": \"pepito\"}]" };
        
        var variableValues = new Dictionary<VariableType, VariableValue>
        {
            { VariableType.Int, intVariableValue },
            { VariableType.Double, doubleVariableValue },
            { VariableType.Str, stringVariableValue },
            { VariableType.Bool, boolVariableValue },
            { VariableType.Bytes, bytesVariableValue },
            { VariableType.JsonArr, jsonArrayVariableValue }
        };
        

        foreach (var variableValue in variableValues)
        {
            var expectedType = variableValue.Key;
            var result = LHMappingHelper.ValueCaseToVariableType(variableValue.Value.ValueCase);
        
            Assert.Equal( expectedType, result);
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
        Assert.Equal(VariableType.Int, result.ReturnType_.Type);
    }

    [Fact]
    public void DotNetTypeToReturnType_WithStringType_ShouldReturnCorrectReturnType()
    {
        var result = LHMappingHelper.DotNetTypeToReturnType(typeof(string));

        Assert.NotNull(result);
        Assert.NotNull(result.ReturnType_);
        Assert.Equal(VariableType.Str, result.ReturnType_.Type);
    }
}