using System;
using System.Collections.Generic;
using LittleHorse.Common.Proto;
using LittleHorse.Sdk.Helper;
using Google.Protobuf.WellKnownTypes;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Tests;
using Xunit;
using Type = System.Type;

public class LHMappingHelperTest
{
    [Fact]
    public void LHHelper_WithSystemIntegralVariableType_ShouldReturnLHVariableIntType()
    {
        var test_allowed_types = new List<Type>() { typeof(Int64), typeof(Int32), typeof(Int16) 
            , typeof(UInt16), typeof(UInt32), typeof(UInt64)
            , typeof(sbyte), typeof(byte), typeof(short), typeof(ushort)
            , typeof(int), typeof(uint), typeof(long) 
            , typeof(ulong), typeof(nint), typeof(nuint)};
        
        foreach (var type in test_allowed_types)
        {
            var result = LHMappingHelper.MapDotNetTypeToLHVariableType(type);
            
            Assert.True(result == VariableType.Int);
        }
    }
    
    [Fact]
    public void LHHelper_WithSystemFloatingVariableType_ShouldReturnLHVariableDoubleType()
    {
        var test_allowed_types = new List<Type>() { typeof(float), typeof(double)};
        
        foreach (var type in test_allowed_types)
        {
            var result = LHMappingHelper.MapDotNetTypeToLHVariableType(type);
            
            Assert.True(result == VariableType.Double);
        }
    }
    
    [Fact]
    public void LHHelper_WithSystemStringVariableType_ShouldReturnLHVariableStrType()
    {
        var type = typeof(String);
        
        var result = LHMappingHelper.MapDotNetTypeToLHVariableType(type);
        
        Assert.True(result == VariableType.Str);
    }
    
    [Fact]
    public void LHHelper_WithSystemBoolVariableType_ShouldReturnLHVariableBoolType()
    {
        var type = typeof(bool);
        
        var result = LHMappingHelper.MapDotNetTypeToLHVariableType(type);
        
        Assert.True(result == VariableType.Bool);
    }
    
    [Fact]
    public void LHHelper_WithSystemBytesVariableType_ShouldReturnLHVariableBytesType()
    {
        var type = typeof(byte[]);
        
        var result = LHMappingHelper.MapDotNetTypeToLHVariableType(type);
        
        Assert.True(result == VariableType.Bytes);
    }
    
    [Fact]
    public void LHHelper_WithSystemArrayObjectVariableType_ShouldReturnLHVariableJsonArrType()
    {
        var test_allowed_types = new List<Type>() { typeof(List<object>), typeof(List<string>), typeof(List<int>)};
        
        foreach (var type in test_allowed_types)
        {
            var result = LHMappingHelper.MapDotNetTypeToLHVariableType(type);
            
            Assert.True(result == VariableType.JsonArr);
        }
    }
    
    [Fact]
    public void LHHelper_WithoutSystemVariableType_ShouldThrowException()
    {
        var test_not_allowed_types = new List<Type>() { typeof(decimal), typeof(char) };
        
        foreach (var type in test_not_allowed_types)
        {
            var exception = Assert.Throws<Exception>(() => LHMappingHelper.MapDotNetTypeToLHVariableType(type));
            
            Assert.Equal($"Unaccepted variable type.", exception.Message);
        }
    }

    [Fact]
    public void LHHelper_WithNullProtoTimestamp_ShouldReturnNull()
    {
        var result = LHMappingHelper.MapDateTimeFromProtoTimeStamp(null!);
        
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
        
        var result = LHMappingHelper.MapDateTimeFromProtoTimeStamp(protoTimestamp);
        
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
        
        var result = LHMappingHelper.MapDateTimeFromProtoTimeStamp(specificTimestamp);
        
        Assert.Equal(specificDateTime, result);
    }

    [Fact]
    public void LHHelper_WithVariableValue_ShouldReturnSameValue()
    {
        VariableValue value = new VariableValue();
        value.Str = "test";
        
        var result = LHMappingHelper.MapObjectToVariableValue(value);
        
        Assert.Equal(value, result);
    }
    
    [Fact]
    public void LHHelper_WithNullLHVariableValue_ShouldThrowException()
    {
        var exception = Assert.Throws<LHInputVarSubstitutionException>
            (() => LHMappingHelper.MapObjectToVariableValue(null));
        
        Assert.Equal($"There is no object to be mapped.", exception.Message);
    }

    [Fact]
    public void LHHelper_WithIntegerValue_ShouldReturnLHIntegerValue()
    {
        int expectedValue = 23;
        var test_int_values = new List<object>() { (sbyte)expectedValue, (byte)expectedValue, (short)expectedValue, 
            (ushort)expectedValue, expectedValue, (uint)expectedValue, (long)expectedValue, (ulong)expectedValue, 
            (nint)expectedValue, (nuint)expectedValue };
        
        foreach (var obj in test_int_values)
        {
            var result = LHMappingHelper.MapObjectToVariableValue(obj);
            
            Assert.Equal(expectedValue, result.Int);
        }
    }

    [Fact]
    public void LHHelper_WithFloatingsValue_ShouldReturnLHDoubleValue()
    {
       var testFloatValues = new List<object>() { 12.3, 3_000.5F, 3D};

       foreach (var value in testFloatValues)
       {
           var result = LHMappingHelper.MapObjectToVariableValue(value);
           
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
        
        var result = LHMappingHelper.MapObjectToVariableValue(stringValue);
        
        Assert.Equal(stringValue, result.Str);
    }
    
    [Fact]
    public void LHHelper_WithBoolValue_ShouldReturnLHBoolValue()
    {
        var boolValue = true;
        
        var result = LHMappingHelper.MapObjectToVariableValue(boolValue);
        
        Assert.Equal(boolValue, result.Bool);
    }
    
    [Fact]
    public void LHHelper_WithBytesValue_ShouldReturnLHBytesValue()
    {
        var bytes = new byte[] { 0x20, 0x20, 0x20 };
        
        var result = LHMappingHelper.MapObjectToVariableValue(bytes);
        
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
        
        var result = LHMappingHelper.MapObjectToVariableValue(persons);
        
        Assert.Contains("\"Age\":36", result.JsonArr);
        Assert.Contains("\"FirstName\":\"Test2\"", result.JsonArr);
    }
    
    [Fact]
    public void LHHelper_WithCustomObjectValue_ShouldReturnLHJsonObjValue()
    {
        var car = new Car {Id = 1, Cost = 134.45E-2f};
        var person = new Person() { Age = 36, Cars = new List<Car>() {car}, FirstName = "Test"};
        
        var result = LHMappingHelper.MapObjectToVariableValue(person);
        
        Assert.Contains("\"FirstName\":\"Test\"", result.JsonObj);
    }
}