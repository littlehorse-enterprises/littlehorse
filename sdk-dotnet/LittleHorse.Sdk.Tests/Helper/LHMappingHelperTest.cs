using System;
using System.Collections.Generic;
using LittleHorse.Common.Proto;
using LittleHorse.Sdk.Helper;
using Google.Protobuf.WellKnownTypes;
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
    public void LHHelper_WithSystemArrayObjectVariableType_ShouldReturnLHVariableBytesType()
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
        DateTime currentDatetimeWithoutSeconds = new DateTime(result!.Value.Year, result.Value.Month, result.Value.Day, result.Value.Hour, result.Value.Minute, 0);
        Assert.Equal(expectedDatetimeWithoutSeconds, currentDatetimeWithoutSeconds);
    }
    
    [Fact]
    public void LHHelper_WithSpecificProtoTimestamp_ShouldReturnSpecificDateTime()
    {
        DateTime specificDateTime = new DateTime(2024, 08, 16, 13, 0, 0, DateTimeKind.Utc);
        
        Timestamp specificTimestamp = Timestamp.FromDateTime(specificDateTime);
        
        var result = LHMappingHelper.MapDateTimeFromProtoTimeStamp(specificTimestamp);
        
        Assert.Equal(specificDateTime, result);
    }
}