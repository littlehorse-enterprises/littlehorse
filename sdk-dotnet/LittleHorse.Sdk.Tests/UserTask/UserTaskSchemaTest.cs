using System;
using System.Collections.Generic;
using System.Text.Json.Nodes;
using LittleHorse.Sdk.UserTask;
using Xunit;

namespace LittleHorse.Sdk.Tests.UserTask;

public class UserTaskSchemaTest
{
    [Fact]
    public void UserTaskSchema_WithUTFieldsOfSupportedPrimitiveTypesInForm_ShouldCompile()
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