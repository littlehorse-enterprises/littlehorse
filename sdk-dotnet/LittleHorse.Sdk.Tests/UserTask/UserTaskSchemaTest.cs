using System;
using System.Collections.Generic;
using System.Text.Json.Nodes;
using LittleHorse.Sdk.UserTask;
using LittleHorse.Sdk.Common.Proto;
using Xunit;

namespace LittleHorse.Sdk.Tests.UserTask;

public class UserTaskSchemaTest
{
    [Fact]
    public void StructBackedSchema_ShouldUseResolvedIdWithoutLegacyFields()
    {
        var id = new StructDefId { Name = "customer", Version = 3 };
        var schema = new UserTaskSchema(id, "customer-data");
        id.Version = 4;

        var request = schema.Compile();
        Assert.Equal("customer-data", request.Name);
        Assert.Equal(new StructDefId { Name = "customer", Version = 3 }, request.ResultStructDefId);
        Assert.Empty(request.Fields);
        request.ResultStructDefId.Version = 5;
        Assert.Equal(3, schema.Compile().ResultStructDefId.Version);
    }

    [Theory]
    [InlineData("", 0, "task")]
    [InlineData("customer", -1, "task")]
    [InlineData("customer", 0, " ")]
    public void StructBackedSchema_ShouldRejectUnresolvedSchemaOrEmptyNames(string name, int version, string task)
    {
        Assert.Throws<ArgumentException>(() =>
            new UserTaskSchema(new StructDefId { Name = name, Version = version }, task));
    }

    [Fact]
    public void StructBackedSchema_ShouldRejectNullId()
    {
        Assert.Throws<ArgumentNullException>(() => new UserTaskSchema((StructDefId)null!, "task"));
    }

    [Fact]
    public void UserTaskSchema_WithUTFieldsOfSupportedVariableTypesInForm_ShouldCompile()
    {
        var userTaskDefName = "customer-data";
        var userTaskSchema = new UserTaskSchema(new CustomerForm(), userTaskDefName);
        var putUserTaskDefRequest = userTaskSchema.Compile();
        
        var expectedNumberOfFieldsInForm = 3;
        Assert.True(expectedNumberOfFieldsInForm == putUserTaskDefRequest.Fields.Count);
        Assert.Equal(userTaskDefName, putUserTaskDefRequest.Name);
    }
    
    [Theory]
    [MemberData(nameof(CustomData.TestValues), MemberType = typeof(CustomData))]
    public void UserTaskSchema_WithUTFieldsNoSupportedTypesInForm_ShouldThrowArgumentException(
        object taskObject, string userTaskDefName)
    {
        var userTaskSchema = new UserTaskSchema(taskObject, userTaskDefName);
        
        var exception = Assert.Throws<ArgumentException>(() => 
            userTaskSchema.Compile());
            
        Assert.Contains("Only primitive types supported for UserTaskField.", exception.Message);
    }

    class CustomerForm
    {
        [UserTaskField(
            DisplayName = "Complete Name", 
            Description = "Your names and last names.")]
        public string Name = "";
    
        [UserTaskField(
            DisplayName = "are you student?", 
            Description = "Enter true or false if you are studying.")]
        public bool IsStudent = false;
        
        [UserTaskField(
            DisplayName = "Age", 
            Description = "Enter your age.")]
        public int Age = 0;
    }
    
    class TestWithNoSupportedTypesForm1
    {
        [UserTaskField(
            DisplayName = "Any display name")]
        public JsonArray Field = new JsonArray();
    }
    
    class TestWithNoSupportedTypesForm2
    {
        [UserTaskField(
            DisplayName = "Any display name")]
        public JsonObject Field = new JsonObject();
    }
    
    class TestWithNoSupportedTypesForm3
    {
        [UserTaskField(
            DisplayName = "Any display name")]
        public byte[] Field = new byte[] {};
    }
    
    class CustomData
    {
        public static IEnumerable<object[]> TestValues =>
            new List<object[]>
            {
                new object[] { new TestWithNoSupportedTypesForm1(), "user-task-def-name1" },
                new object[] { new TestWithNoSupportedTypesForm2(), "user-task-def-name2" },
                new object[] { new TestWithNoSupportedTypesForm3(), "user-task-def-name3" }
            };
    }
}