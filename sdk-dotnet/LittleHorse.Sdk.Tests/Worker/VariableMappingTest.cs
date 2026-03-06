using System;
using System.Collections.Generic;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using Type = System.Type;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Worker;
using Moq;
using Newtonsoft.Json;
using Xunit;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk.Tests.Worker;

public class VariableMappingTest
{
    public VariableMappingTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
    }
    
    [Fact]
    public void VariableMapping_WithValidLHTypes_ShouldBeBuiltSuccessfully()
    {
        int position = 0;
        string paramName = "param_test";
        
        var testAllowedTypes = new List<Type>() 
        {
            typeof(Int64), typeof(Int32), typeof(Int16) 
            , typeof(UInt16), typeof(UInt32), typeof(UInt64)
            , typeof(sbyte), typeof(byte), typeof(short), typeof(ushort)
            , typeof(int), typeof(uint), typeof(long) 
            , typeof(ulong), typeof(nint), typeof(nuint), typeof(float), typeof(double)
            , typeof(string), typeof(bool), typeof(byte[]), typeof(List<string>)
            , typeof(List<object>), typeof(List<string>), typeof(List<int>)
        };

        foreach (var type in testAllowedTypes)
        {
            var variableType = LHMappingHelper.DotNetTypeToLHVariableType(type);
            TaskDef? taskDef = GetTaskDefForTest(variableType);
        
            var result = new VariableMapping(taskDef!, position, type, paramName);
        
            Assert.True(result is not null);   
        }
    }

    [Fact]
    public void VariableMapping_WithMismatchTypesInt_ShouldThrowException()
    {
        Type type1 = typeof(Int64);
        Type type2 = typeof(string);
        var variableType = LHMappingHelper.DotNetTypeToLHVariableType(type1);
        TaskDef? taskDef = GetTaskDefForTest(variableType);
        
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(
            () => new VariableMapping(taskDef!, 0, type2, "any param name"));
            
        Assert.Contains("TaskDef provides INT, func accepts", exception.Message);
    }
    
    [Fact]
    public void VariableMapping_WithMismatchTypeDouble_ShouldThrowException()
    {
        Type type1 = typeof(double);
        Type type2 = typeof(Int64);
        var variableType = LHMappingHelper.DotNetTypeToLHVariableType(type1);
        TaskDef? taskDef = GetTaskDefForTest(variableType);
        
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(
            () => new VariableMapping(taskDef!, 0, type2, "any param name"));
            
        Assert.Contains("TaskDef provides DOUBLE, func accepts", exception.Message);
    }
    
    [Fact]
    public void VariableMapping_WithMismatchTypeString_ShouldThrowException()
    {
        Type type1 = typeof(string);
        Type type2 = typeof(double);
        var variableType = LHMappingHelper.DotNetTypeToLHVariableType(type1);
        TaskDef? taskDef = GetTaskDefForTest(variableType);
        
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(
            () => new VariableMapping(taskDef!, 0, type2, "any param name"));
            
        Assert.Contains("TaskDef provides STRING, func accepts", exception.Message);
    }

    [Fact]
    public void VariableMapping_WithMismatchTypeBool_ShouldThrowException()
    {
        Type type1 = typeof(bool);
        Type type2 = typeof(string);
        var variableType = LHMappingHelper.DotNetTypeToLHVariableType(type1);
        TaskDef? taskDef = GetTaskDefForTest(variableType);
        
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(
            () => new VariableMapping(taskDef!, 0, type2, "any param name"));
            
        Assert.Contains("TaskDef provides BOOL, func accepts", exception.Message);
    }
    
    [Fact]
    public void VariableMapping_WithMismatchTypeBytes_ShouldThrowException()
    {
        Type type1 = typeof(byte[]);
        Type type2 = typeof(string);
        var variableType = LHMappingHelper.DotNetTypeToLHVariableType(type1);
        TaskDef? taskDef = GetTaskDefForTest(variableType);
        
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(
            () => new VariableMapping(taskDef!, 0, type2, "any param name"));
            
        Assert.Contains("TaskDef provides BYTES, func accepts", exception.Message);
    }
    
    [Fact]
    public void VariableMapping_WithAssignIntValue_ShouldReturnInt32Object()
    {
        int expectedValue = 29;
        var testAllowedTypes = new List<Type>() { typeof(Int32), typeof(Int16) 
            , typeof(UInt16), typeof(UInt32)
            , typeof(sbyte), typeof(byte), typeof(short), typeof(ushort)
            , typeof(int), typeof(uint)
            , typeof(nint), typeof(nuint)};
        
        int position = 0;
        string paramName = "param_test";

        var mockClient = new Mock<LittleHorseClient>();

        foreach (var type in testAllowedTypes)
        {
            var variableMapping = GetVariableMappingForTest(type, paramName, position);
            VariableValue variableValue = new VariableValue {Int = expectedValue};
            ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);

            var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
            var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        
            Assert.Equal((int) taskInstance.Variables[0].Value.Int, result);
        }
    }
    
    [Fact]
    public void VariableMapping_WithNoValue_ShouldReturnNull()
    {
        Type type = typeof(string);
        int position = 0;
        string paramName = "param_test";
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue();
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);

        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);

        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);

        Assert.Null(result);
    }

    [Fact]
    public void VariableMapping_WithAssignLongValue_ShouldReturnInt64Object()
    {
        int expectedValue = 29;
        var testAllowedTypes = new List<Type>() { typeof(Int64), typeof(UInt64), typeof(long), typeof(ulong)};
        
        int position = 0;
        string paramName = "param_test";

        var mockClient = new Mock<LittleHorseClient>();

        foreach (var type in testAllowedTypes)
        {
            var variableMapping = GetVariableMappingForTest(type, paramName, position);
            VariableValue variableValue = new VariableValue {Int = expectedValue};
            ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);
            var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
            var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        
            Assert.Equal(taskInstance.Variables[0].Value.Int, result);
        }
    }
    
    [Fact]
    public void VariableMapping_WithAssignDoubleValue_ShouldReturnDoubleObject()
    {
        float expectedValue = 3_000.5F;
        var testAllowedTypes = new List<Type>() { typeof(double), typeof(Double)};
        
        int position = 0;
        string paramName = "param_test";

        var mockClient = new Mock<LittleHorseClient>();

        foreach (var type in testAllowedTypes)
        {
            var variableMapping = GetVariableMappingForTest(type, paramName, position);
            VariableValue variableValue = new VariableValue {Double = expectedValue};
            ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);
            
            var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
            var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        
            Assert.Equal(taskInstance.Variables[0].Value.Double, result);
        }
    }
    
    [Fact]
    public void VariableMapping_WithAssignDoubleValue_ShouldReturnFloatObject()
    {
        float expectedValue = 3_000.5F;
        var type = typeof(float);
        
        int position = 0;
        string paramName = "param_test";
        
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue {Double = expectedValue};
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);

        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
    
        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
    
        Assert.Equal((float) taskInstance.Variables[0].Value.Double, result);
    }
    
    [Fact]
    public void VariableMapping_WithAssignStringValue_ShouldReturnStrObject()
    {
        Type type = typeof(string);
        int position = 0;
        string paramName = "param_test";
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue { Str = "param_value_test"};
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);
        
        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        
        Assert.Equal(taskInstance.Variables[0].Value.Str, result);
    }
    
    [Fact]
    public void VariableMapping_WithAssignBytesValue_ShouldReturnBytesObject()
    {
        byte[] expectedValue = new byte[] { 0x20, 0x20, 0x20 };
        ByteString byteString = ByteString.CopyFrom(expectedValue);
        Type type = typeof(byte[]);
        int position = 0;
        string paramName = "param_test";
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue { Bytes = byteString};
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);

        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        
        Assert.Equal(taskInstance.Variables[0].Value.Bytes, result);
    }
    
    [Fact]
    public void VariableMapping_WithAssignBoolValue_ShouldReturnBoolObject()
    {
        bool expectedValue = false;
        Type type = typeof(bool);
        int position = 0;
        string paramName = "param_test";
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue { Bool = expectedValue};
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);
        
        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        
        Assert.Equal(taskInstance.Variables[0].Value.Bool, result);
    }


    [Fact]
    public void VariableMapping_WithAssignTimestampValue_ShouldReturnDateObject()
    {
        DateTime expectedValue = DateTime.UtcNow;
        Type type = typeof(DateTime);
        int position = 0;
        string paramName = "param_test";
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue { UtcTimestamp = expectedValue.ToTimestamp()};
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);
        
        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        Assert.NotNull(result);
        Assert.IsType(type, result);
        Assert.Equal(expectedValue, result);
    }
    
    [Fact]
    public void VariableMapping_WithAssignWfRunIdValue_ShouldReturnWfRunIdObject()
    {
        WfRunId expectedValue = new WfRunId() {Id = "test"};
        Type type = typeof(WfRunId);
        int position = 0;
        string paramName = "param_test";
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue { WfRunId = expectedValue };
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);
        
        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        Assert.NotNull(result);
        Assert.IsType(type, result);
        Assert.Equal(expectedValue, result);
    }
    
    [Fact]
    public void VariableMapping_WithAssignArrayObjectValue_ShouldReturnArrayObject()
    {
        string value = "[{\"FirstName\":\"Test\",\"Age\":35,\"Cars\":[{\"Id\":1,\"Cost\":1.3445}]}]";
        Type type = typeof(List<Person>);
        int position = 0;
        string paramName = "param_test";
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue { JsonArr = value};
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);

        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        
        var expectedList = (List<Person>)JsonConvert.DeserializeObject(value, type)!;
        var actualList = (List<Person>)result!;
        
        Assert.Equal(expectedList.Count, actualList.Count);
        Assert.Equal(expectedList[0].FirstName, actualList[0].FirstName);
        Assert.Equal(expectedList[0].Age, actualList[0].Age);
        Assert.Equal(expectedList[0].Cars!.Count, actualList[0].Cars!.Count);
    }
    
    [Fact]
    public void VariableMapping_WithAssignJsonObjectValue_ShouldReturnDictionaryObject()
    {
        string value = "{\"FirstName\":\"Test\",\"Age\":\"35\",\"Address\":\"NA-Street\"}";
        Type type = typeof(Dictionary<string, string>);
        int position = 0;
        string paramName = "param_test";
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue { JsonObj = value};
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);

        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        
        var expectedList = (Dictionary<string, string>)JsonConvert.DeserializeObject(value, type)!;
        var actualList = (Dictionary<string, string>)result!;
        
        Assert.Equal(expectedList.Count, actualList.Count);
        Assert.Equal(expectedList["FirstName"], actualList["FirstName"]);
        Assert.Equal(expectedList["Age"], actualList["Age"]);
        Assert.Equal(expectedList["Address"], actualList["Address"]);
    }
    
    [Fact]
    public void VariableMapping_WithAssignJsonStringValue_ShouldReturnCustomObject()
    {
        string value = "{\"FirstName\":\"Test\",\"Age\":35,\"Cars\":[{\"Id\":1,\"Cost\":1.3445}]}";
        Type type = typeof(Person);
        int position = 0;
        string paramName = "param_test";
        var variableMapping = GetVariableMappingForTest(type, paramName, position);
        VariableValue variableValue = new VariableValue { JsonObj = value};
        ScheduledTask taskInstance = GetScheduledTaskForTest(variableValue, paramName);

        var mockClient = new Mock<LittleHorseClient>();
        var mockWorkerContext = new Mock<LHWorkerContext>(taskInstance, new DateTime(), mockClient.Object);
        
        var result = variableMapping.Assign(taskInstance, mockWorkerContext.Object);
        
        var expectedObject = (Person)JsonConvert.DeserializeObject(value, type)!;
        var actualObject = (Person)result!;
        
        Assert.Equal(expectedObject.FirstName, actualObject.FirstName);
        Assert.Equal(expectedObject.Age, actualObject.Age);
        Assert.Equal(expectedObject.Cars!.Count, actualObject.Cars!.Count);
    }

    private TaskDef? GetTaskDefForTest(VariableType type)
    {
        var inputVar = new VariableDef();
        inputVar.Type = type;
        TaskDef? taskDef = new TaskDef();
        TaskDefId taskDefId = new TaskDefId();
        taskDef.Id = taskDefId;
        taskDef.InputVars.Add(inputVar);
        
        return taskDef;
    }

    private VariableMapping GetVariableMappingForTest(Type type, string paramName, int position)
    {
        var variableType = LHMappingHelper.DotNetTypeToLHVariableType(type);
        TaskDef? taskDef = GetTaskDefForTest(variableType);
        
        var variableMapping = new VariableMapping(taskDef!, position, type, paramName);
        
        return variableMapping;
    }

    private ScheduledTask GetScheduledTaskForTest(VariableValue variableValue, string variableName)
    {
        ScheduledTask scheduledTask = new ScheduledTask();
        List<VarNameAndVal> variables = new List<VarNameAndVal>();
        variables.Add(new VarNameAndVal {VarName = variableName, Value = variableValue, Masked = true});
        scheduledTask.Variables.Add(variables);

        return scheduledTask;
    }
}