using System.Collections.Generic;
using LittleHorse.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Worker.Internal;
using Xunit;

public class LHTaskSignatureTest
{
    const string TASK_DEF_NAME_ADD = "add-task-worker";
    const string TASK_DEF_NAME_UPDATE = "update-task-worker";
    const string TASK_DEF_NAME_DELETE = "delete-task-worker";
    const string TASK_DEF_NAME_GET = "get-task-worker";
    const string TASK_DEF_NAME_VALIDATE = "validate-task-worker";
    const string TASK_DEF_NAME_SHOW = "show-task-worker";
    const string TASK_DEF_NAME_PROCESS = "process-task-worker";
    const string DEFAULT_OUTPUT_VARIABLE_NAME = "output";
    const bool TRUE_IS_MASKET = true;
    const string VALUE_ATTR_NAME = "value";
    const string DETAIL_ATTR_NAME = "detail";
    const string TELEPHONE_ATTR_NAME = "telephone";
    
    [Fact]
    public void TaskSignature_WithLHTaskMethodAndLHTypeAttributes_ShouldBuildLHSignatureWithInputAndOutput()
    {
        int number_of_method_params = 1;
        
        var taskSignature = new LHTaskSignature<TestWorker>(TASK_DEF_NAME_ADD, new TestWorker());
        
        var expectedLHMethodParam = new LHMethodParam
        {
            Type = VariableType.Str,
            Name = "name",
            IsMasked = TRUE_IS_MASKET
        };
        var expectedOutput = new TaskDefOutputSchema
        {
            ValueDef = new VariableDef
            {
                Name = DEFAULT_OUTPUT_VARIABLE_NAME,
                Type = VariableType.Str,
                MaskedValue = TRUE_IS_MASKET
            }
        };

        Assert.True(taskSignature.LhMethodParams.Count == number_of_method_params);
        foreach (var actualLHMethodParam in taskSignature.LhMethodParams)
        {
            Assert.Equal(expectedLHMethodParam.Name, actualLHMethodParam.Name);
            Assert.Equal(expectedLHMethodParam.Type, actualLHMethodParam.Type);
            Assert.Equal(expectedLHMethodParam.IsMasked, actualLHMethodParam.IsMasked);
        }
        
        Assert.Equal(expectedOutput.ValueDef.Name, taskSignature.TaskDefOutputSchema!.ValueDef.Name);
        Assert.Equal(expectedOutput.ValueDef.Type, taskSignature.TaskDefOutputSchema.ValueDef.Type);
        Assert.Equal(expectedOutput.ValueDef.MaskedValue, taskSignature.TaskDefOutputSchema.ValueDef.MaskedValue);
        Assert.False(taskSignature.HasWorkerContextAtEnd);
    }
    
    [Fact]
    public void TaskSignature_WithLHTaskMethodAndLHTypeAttributes_ShouldBuildSignatureWithOutputResult()
    {
        int number_of_method_params = 1;
        
        var taskSignature = new LHTaskSignature<TestWorker>(TASK_DEF_NAME_UPDATE, new TestWorker());
        
        var expectedOutput = new TaskDefOutputSchema
        {
            ValueDef = new VariableDef
            {
                Name = "result",
                Type = VariableType.Str,
                MaskedValue = TRUE_IS_MASKET
            }
        };

        Assert.True(taskSignature.LhMethodParams.Count == number_of_method_params); ;
        foreach (var actualLHMethodParam in taskSignature.LhMethodParams)
        {
            Assert.False(actualLHMethodParam.IsMasked);
        }
        
        Assert.Equal(expectedOutput.ValueDef.Name, taskSignature.TaskDefOutputSchema!.ValueDef.Name);
        Assert.Equal(expectedOutput.ValueDef.Type, taskSignature.TaskDefOutputSchema.ValueDef.Type);
        Assert.Equal(expectedOutput.ValueDef.MaskedValue, taskSignature.TaskDefOutputSchema.ValueDef.MaskedValue);
        Assert.False(taskSignature.HasWorkerContextAtEnd);
    }
    
    [Fact]
    public void TaskSignature_WithLHContextWorkerAtEndOfMethodParams_ShouldBuildSignatureSuccessfully()
    {
        int number_of_method_params = 1;
        var taskSignature = new LHTaskSignature<TestWorker>(TASK_DEF_NAME_DELETE, new TestWorker());

        Assert.True(taskSignature.LhMethodParams.Count == number_of_method_params); ;
        Assert.True(taskSignature.HasWorkerContextAtEnd);
    }
    
    [Fact]
    public void TaskSignature_WithLHContextWorkerFirstInMethodParams_ShouldThrowAnException()
    {
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(() => new LHTaskSignature<TestWorker>(TASK_DEF_NAME_VALIDATE, new TestWorker()));
            
        Assert.Equal("Can only have WorkerContext as the last parameter.", exception.Message);
    }
    
    [Fact]
    public void TaskSignature_WithLHContextWorkerInMiddleOfMethodParams_ShouldThrowAnException()
    {
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(() => new LHTaskSignature<TestWorker>(TASK_DEF_NAME_GET, new TestWorker()));
            
        Assert.Equal("Can only have WorkerContext as the last parameter.", exception.Message);
    }
    
    [Fact]
    public void TaskSignature_WithoutLHCustomAttributes_ShouldThrowAnException()
    {
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(() => 
            new LHTaskSignature<TestWorker>("Unknown task def", new TestWorker()));
            
        Assert.Contains("Couldn't find [LHTaskMethod] attribute for taskDef", exception.Message);
    }
    
    [Fact]
    public void TaskSignature_WithMoreThanOneLHTaskMethodAndSameTaskDefName_ShouldThrowAnException()
    {
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(() => 
            new LHTaskSignature<TestWorker>(TASK_DEF_NAME_PROCESS, new TestWorker()));
            
        Assert.Equal("Found more than one annotated task methods!", exception.Message);
    }
    
    [Fact]
    public void TaskSignature_WithManyMaskedInputParams_ShouldBuildLHSignatureWithInputAndOutput()
    {
        int number_of_method_params = 3;
        
        var taskSignature = new LHTaskSignature<TestWorker>(TASK_DEF_NAME_SHOW, new TestWorker());

        List<LHMethodParam> actualLHMethodParams = taskSignature.LhMethodParams;
        List<LHMethodParam> expectedLHMethodParams = new List<LHMethodParam>
        {
            new LHMethodParam
            {
                Type = VariableType.Int,
                Name = VALUE_ATTR_NAME,
                IsMasked = TRUE_IS_MASKET
            },
            new LHMethodParam
            {
                Type = VariableType.Str,
                Name = DETAIL_ATTR_NAME,
                IsMasked = false
            },
            new LHMethodParam
            {
                Type = VariableType.Str,
                Name = TELEPHONE_ATTR_NAME,
                IsMasked = TRUE_IS_MASKET
            }
        };

        var expectedOutput = new TaskDefOutputSchema
        {
            ValueDef = new VariableDef
            {
                Name = DEFAULT_OUTPUT_VARIABLE_NAME,
                Type = VariableType.Str,
                MaskedValue = false
            }
        };

        Assert.True(taskSignature.LhMethodParams.Count == number_of_method_params);
        if (actualLHMethodParams.Count == number_of_method_params && expectedLHMethodParams.Count == number_of_method_params)
        {
            for (int i = 0; i < actualLHMethodParams.Count; i++) 
            {
                Assert.Equal(expectedLHMethodParams[i].Name, actualLHMethodParams[i].Name);
                Assert.Equal(expectedLHMethodParams[i].Type, actualLHMethodParams[i].Type);
                Assert.Equal(expectedLHMethodParams[i].IsMasked, actualLHMethodParams[i].IsMasked);
            }
        }

        Assert.Equal(expectedOutput.ValueDef.Name, taskSignature.TaskDefOutputSchema!.ValueDef.Name);
        Assert.Equal(expectedOutput.ValueDef.Type, taskSignature.TaskDefOutputSchema.ValueDef.Type);
        Assert.Equal(expectedOutput.ValueDef.MaskedValue, taskSignature.TaskDefOutputSchema.ValueDef.MaskedValue);
        Assert.False(taskSignature.HasWorkerContextAtEnd);
    }
    
    class TestWorker
    {
        [LHTaskMethod(TASK_DEF_NAME_ADD)]
        [LHType(masked: TRUE_IS_MASKET)]
        public string Add([LHType(masked: TRUE_IS_MASKET)] string name)
        {
            return $"Output value: {name}";
        }
        
        [LHTaskMethod(TASK_DEF_NAME_UPDATE)]
        [LHType(masked: TRUE_IS_MASKET, name: "result")]
        public string Update(int value)
        {
            return $"Output value: {value}";
        }
        
        [LHTaskMethod(TASK_DEF_NAME_DELETE)]
        public string Delete(float value, LHWorkerContext workerContext)
        {
            workerContext.Log($"Output value: {value}");
            return $"Output value: {value}";
        }
        
        [LHTaskMethod(TASK_DEF_NAME_GET)]
        public string Get(float value, string description, LHWorkerContext workerContext, string phone)
        {
            workerContext.Log($"Output value: {value}");
            return $"Output value: {value}, Description: {description} and Phone: {phone}";
        }
        
        [LHTaskMethod(TASK_DEF_NAME_VALIDATE)]
        public string Validate(LHWorkerContext workerContext, float value, string description, string phone)
        {
            workerContext.Log($"Output value: {value}");
            return $"Output value: {value}, Description: {description} and Phone: {phone}";
        }
        
        [LHTaskMethod(TASK_DEF_NAME_SHOW)]
        public string Show([LHType(masked: true, name: VALUE_ATTR_NAME)] int value, [LHType(masked: false, name: DETAIL_ATTR_NAME)] string description, [LHType(masked: true, name:TELEPHONE_ATTR_NAME)] string phone)
        {
            return $"Output value: {value}, Description: {description} and Phone: {phone}";
        }
        
        [LHTaskMethod(TASK_DEF_NAME_PROCESS)]
        public string ProcessPayment(float cost)
        {
            return $"Output value: {cost}";
        }
        
        [LHTaskMethod(TASK_DEF_NAME_PROCESS)]
        public string ProcessOrder(string account_number)
        {
            return $"Output value: {account_number}";
        }
    }
}
