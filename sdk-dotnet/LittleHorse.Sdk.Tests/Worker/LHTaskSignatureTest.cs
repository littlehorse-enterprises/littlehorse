using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Worker;
using Xunit;

public class LHTaskSignatureTest
{
    const string TASK_DEF_NAME_ADD = "add-task-worker";
    const string TASK_DEF_NAME_INFORM = "inform-task-worker";
    const string TASK_DEF_NAME_UPDATE = "update-task-worker";
    const string TASK_DEF_NAME_DELETE = "delete-task-worker";
    const string TASK_DEF_NAME_GET = "get-task-worker";
    const string TASK_DEF_NAME_VALIDATE = "validate-task-worker";
    const string TASK_DEF_NAME_SHOW = "show-task-worker";
    const string TASK_DEF_NAME_PROCESS = "process-task-worker";
    const string TASK_DEF_NAME_DESCRIBE = "describe-task-worker";
    const string DEFAULT_OUTPUT_VARIABLE_NAME = "output";
    const bool TRUE_IS_MASKED = true;
    const string VALUE_ATTR_NAME = "value";
    const string DETAIL_ATTR_NAME = "detail";
    const string TELEPHONE_ATTR_NAME = "telephone";
    const string TASK_DEF_DESCRIPTION = "Adds description metadata";
    
    public LHTaskSignatureTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
    }
    
    [Fact]
    public void TaskSignature_WithLHTaskMethodAndLHTypeAttributes_ShouldBuildLHSignatureWithInputAndOutput()
    {
        int number_of_method_params = 1;
        
        var taskSignature = new LHTaskSignature<TestWorker>(TASK_DEF_NAME_ADD, new TestWorker());
        
        var expectedLHMethodParam = new VariableDef
        {
            Name = "name",
            TypeDef = new TypeDefinition
            {
                PrimitiveType = VariableType.Str,
                Masked = TRUE_IS_MASKED
            }
        };
        var expectedOutput = new ReturnType{
            ReturnType_ = new TypeDefinition{
                PrimitiveType = VariableType.Str,
                Masked = TRUE_IS_MASKED
            }
        };

        Assert.True(taskSignature.VariableDefs.Count == number_of_method_params);
        foreach (var actualLHMethodParam in taskSignature.VariableDefs)
        {
            Assert.Equal(expectedLHMethodParam.Name, actualLHMethodParam.Name);
            Assert.Equal(expectedLHMethodParam.TypeDef, actualLHMethodParam.TypeDef);
        }
        
        Assert.Equal(expectedOutput, taskSignature.ReturnType);
        Assert.False(taskSignature.HasWorkerContextAtEnd);
    }
    
    [Fact]
    public void TaskSignature_WithoutReturnTypeInLHTaskMethod_ShouldBuildLHSignatureWithoutSchemaOutput()
    {
        int number_of_method_params = 1;
        
        var taskSignature = new LHTaskSignature<TestWorker>(TASK_DEF_NAME_INFORM, new TestWorker());
        
        var expectedLHMethodParam = new VariableDef
        {
            Name = "name",
            TypeDef = new TypeDefinition
            {
                PrimitiveType = VariableType.Str,
                Masked = TRUE_IS_MASKED
            }
        };

        Assert.True(taskSignature.VariableDefs.Count == number_of_method_params);
        foreach (var actualLHMethodParam in taskSignature.VariableDefs)
        {
            Assert.Equal(expectedLHMethodParam.Name, actualLHMethodParam.Name);
            Assert.Equal(expectedLHMethodParam.TypeDef, actualLHMethodParam.TypeDef);
        }
        
        Assert.Equal(taskSignature.ReturnType, new ReturnType{});
        Assert.False(taskSignature.HasWorkerContextAtEnd);
    }
    
    [Fact]
    public void TaskSignature_WithLHTaskMethodAndLHTypeAttributes_ShouldBuildSignatureWithOutputResult()
    {
        int number_of_method_params = 1;
        
        var taskSignature = new LHTaskSignature<TestWorker>(TASK_DEF_NAME_UPDATE, new TestWorker());
        
        var expectedOutput = new ReturnType
        {
            ReturnType_ = new TypeDefinition
            {
                PrimitiveType = VariableType.Str,
                Masked = TRUE_IS_MASKED
            }
        };

        Assert.True(taskSignature.VariableDefs.Count == number_of_method_params); ;
        foreach (var actualLHMethodParam in taskSignature.VariableDefs)
        {
            Assert.False(actualLHMethodParam.TypeDef.Masked);
        }
        
        Assert.Equal(expectedOutput, taskSignature.ReturnType);
        Assert.False(taskSignature.HasWorkerContextAtEnd);
    }
    
    [Fact]
    public void TaskSignature_WithLHContextWorkerAtEndOfMethodParams_ShouldBuildSignatureSuccessfully()
    {
        int number_of_method_params = 1;
        var taskSignature = new LHTaskSignature<TestWorker>(TASK_DEF_NAME_DELETE, new TestWorker());

        Assert.True(taskSignature.VariableDefs.Count == number_of_method_params); ;
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

        List<VariableDef> actualLHMethodParams = taskSignature.VariableDefs;
        List<VariableDef> expectedLHMethodParams = new List<VariableDef>
        {
            new VariableDef
            {
                Name = VALUE_ATTR_NAME,
                TypeDef = new TypeDefinition
                {
                    PrimitiveType = VariableType.Int,
                    Masked = TRUE_IS_MASKED
                }
            },
            new VariableDef
            {
                Name = DETAIL_ATTR_NAME,
                TypeDef = new TypeDefinition
                {
                    PrimitiveType = VariableType.Str,
                    Masked = false
                }
            },
            new VariableDef
            {
                Name = TELEPHONE_ATTR_NAME,
                TypeDef = new TypeDefinition
                {
                    PrimitiveType = VariableType.Str,
                    Masked = TRUE_IS_MASKED
                }
            }
        };

        var expectedOutput = new ReturnType
        {
            ReturnType_ = new TypeDefinition
            {
                PrimitiveType = VariableType.Str,
                Masked = false
            }
        };

        Assert.True(taskSignature.VariableDefs.Count == number_of_method_params);
        if (actualLHMethodParams.Count == number_of_method_params && expectedLHMethodParams.Count == number_of_method_params)
        {
            for (int i = 0; i < actualLHMethodParams.Count; i++) 
            {
                Assert.Equal(expectedLHMethodParams[i].Name, actualLHMethodParams[i].Name);
                Assert.Equal(expectedLHMethodParams[i].TypeDef, actualLHMethodParams[i].TypeDef);
            }
        }

        Assert.Equal(expectedOutput, taskSignature.ReturnType);
        Assert.False(taskSignature.HasWorkerContextAtEnd);
    }

    [Fact]
    public void TaskSignature_WithDescription_ShouldExposeTaskDefDescription()
    {
        var taskSignature = new LHTaskSignature<TestWorker>(TASK_DEF_NAME_DESCRIBE, new TestWorker());

        Assert.Equal(TASK_DEF_DESCRIPTION, taskSignature.TaskDefDescription);
    }

    // ──────────────────────────────────────────────────────────────
    // LH Array parameter tests (mirrors LHTaskParameterTest.java)
    // ──────────────────────────────────────────────────────────────

    [Fact]
    public void TaskSignature_WithNativeArrayParam_ShouldProduceInlineArrayDef()
    {
        var taskSignature = new LHTaskSignature<TestWorker>("native-arr-param", new TestWorker());

        var expectedVariableDef = new VariableDef
        {
            Name = "words",
            TypeDef = new TypeDefinition
            {
                InlineArrayDef = new InlineArrayDef
                {
                    ArrayType = new TypeDefinition { PrimitiveType = VariableType.Str }
                }
            }
        };

        Assert.Single(taskSignature.VariableDefs);
        Assert.Equal(expectedVariableDef, taskSignature.VariableDefs[0]);
    }

    [Fact]
    public void TaskSignature_WithNativeArrayListParam_ShouldProduceInlineArrayDef()
    {
        var taskSignature = new LHTaskSignature<TestWorker>("native-arr-list-param", new TestWorker());

        var expectedVariableDef = new VariableDef
        {
            Name = "words",
            TypeDef = new TypeDefinition
            {
                InlineArrayDef = new InlineArrayDef
                {
                    ArrayType = new TypeDefinition { PrimitiveType = VariableType.Str }
                }
            }
        };

        Assert.Single(taskSignature.VariableDefs);
        Assert.Equal(expectedVariableDef, taskSignature.VariableDefs[0]);
    }

    [Fact]
    public void TaskSignature_WithJsonArrayParam_ShouldProducePrimitiveJsonArr()
    {
        var taskSignature = new LHTaskSignature<TestWorker>("json-arr-param", new TestWorker());

        var expectedVariableDef = new VariableDef
        {
            Name = "words",
            TypeDef = new TypeDefinition { PrimitiveType = VariableType.JsonArr }
        };

        Assert.Single(taskSignature.VariableDefs);
        Assert.Equal(expectedVariableDef, taskSignature.VariableDefs[0]);
    }

    [Fact]
    public void TaskSignature_WithIsLHArrayOnNonArrayParam_ShouldThrow()
    {
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(
            () => new LHTaskSignature<TestWorker>("non-array-lh-array-param", new TestWorker()));

        Assert.Contains("@LHType(isLHArray = true) can only be used on array or IList<T> parameters", exception.Message);
    }

    [Fact]
    public void TaskSignature_WithIsLHArrayOnBytesParam_ShouldThrow()
    {
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(
            () => new LHTaskSignature<TestWorker>("bytes-lh-array-param", new TestWorker()));

        Assert.Contains("Cannot use @LHType(isLHArray = true) with byte[]", exception.Message);
    }

    [Fact]
    public void TaskSignature_WithIsLHArrayAndJsonObjElementParam_ShouldThrow()
    {
        // UnannotatedPoco resolves to JSON_OBJ — forbidden inside a native LH Array.
        Assert.Throws<ArgumentException>(
            () => new LHTaskSignature<TestWorker>("invalid-native-arr-jsonobj-param", new TestWorker()));
    }

    // ──────────────────────────────────────────────────────────────
    // LH Array return type tests (mirrors LHTaskReturnTypeTest.java)
    // ──────────────────────────────────────────────────────────────

    [Fact]
    public void TaskSignature_WithNativeArrayReturnType_ShouldProduceInlineArrayDef()
    {
        var taskSignature = new LHTaskSignature<TestWorker>("native-arr-return", new TestWorker());

        var expectedReturnType = new ReturnType
        {
            ReturnType_ = new TypeDefinition
            {
                InlineArrayDef = new InlineArrayDef
                {
                    ArrayType = new TypeDefinition { PrimitiveType = VariableType.Str }
                }
            }
        };

        Assert.Equal(expectedReturnType, taskSignature.ReturnType);
    }

    [Fact]
    public void TaskSignature_WithJsonArrayReturnType_ShouldProducePrimitiveJsonArr()
    {
        var taskSignature = new LHTaskSignature<TestWorker>("json-arr-return", new TestWorker());

        var expectedReturnType = new ReturnType
        {
            ReturnType_ = new TypeDefinition { PrimitiveType = VariableType.JsonArr }
        };

        Assert.Equal(expectedReturnType, taskSignature.ReturnType);
    }

    [Fact]
    public void TaskSignature_WithIsLHArrayOnNonArrayReturnType_ShouldThrow()
    {
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(
            () => new LHTaskSignature<TestWorker>("non-array-lh-array-return", new TestWorker()));

        Assert.Contains("@LHType(isLHArray = true) can only be used on array or IList<T> return types", exception.Message);
    }

    [Fact]
    public void TaskSignature_WithIsLHArrayOnBytesReturnType_ShouldThrow()
    {
        var exception = Assert.Throws<LHTaskSchemaMismatchException>(
            () => new LHTaskSignature<TestWorker>("bytes-lh-array-return", new TestWorker()));

        Assert.Contains("Cannot use @LHType(isLHArray = true) with byte[]", exception.Message);
    }

    class TestWorker
    {
        [LHTaskMethod(TASK_DEF_NAME_ADD)]
        [LHType(masked: TRUE_IS_MASKED)]
        public Task<string> Add([LHType(masked: TRUE_IS_MASKED)] string name)
        {
            return Task.FromResult($"Output value: {name}");
        }
        
        [LHTaskMethod(TASK_DEF_NAME_INFORM)]
        public Task Inform([LHType(masked: TRUE_IS_MASKED)] string name)
        {
            var test_variable = "test_variable" + name;
            return Task.CompletedTask;
        }
        
        [LHTaskMethod(TASK_DEF_NAME_UPDATE)]
        [LHType(masked: TRUE_IS_MASKED, name: "result")]
        public Task<string> Update(int value)
        {
            return Task.FromResult($"Output value: {value}");
        }
        
        [LHTaskMethod(TASK_DEF_NAME_DELETE)]
        public Task<string> Delete(float value, LHWorkerContext workerContext)
        {
            workerContext.Log($"Output value: {value}");
            return Task.FromResult($"Output value: {value}");
        }
        
        [LHTaskMethod(TASK_DEF_NAME_GET)]
        public Task<string> Get(float value, string description, LHWorkerContext workerContext, string phone)
        {
            workerContext.Log($"Output value: {value}");
            return Task.FromResult($"Output value: {value}, Description: {description} and Phone: {phone}");
        }
        
        [LHTaskMethod(TASK_DEF_NAME_VALIDATE)]
        public Task<string> Validate(LHWorkerContext workerContext, float value, string description, string phone)
        {
            workerContext.Log($"Output value: {value}");
            return Task.FromResult($"Output value: {value}, Description: {description} and Phone: {phone}");
        }
        
        [LHTaskMethod(TASK_DEF_NAME_SHOW)]
        public Task<string> Show([LHType(masked: true, name: VALUE_ATTR_NAME)] int value, [LHType(masked: false, name: DETAIL_ATTR_NAME)] string description, [LHType(masked: true, name:TELEPHONE_ATTR_NAME)] string phone)
        {
            return Task.FromResult($"Output value: {value}, Description: {description} and Phone: {phone}");
        }

        [LHTaskMethod(TASK_DEF_NAME_DESCRIBE, TASK_DEF_DESCRIPTION)]
        public Task<string> Describe(string name)
        {
            return Task.FromResult($"Description: {name}");
        }
        
        [LHTaskMethod(TASK_DEF_NAME_PROCESS)]
        public Task<string> ProcessPayment(float cost)
        {
            return Task.FromResult($"Output value: {cost}");
        }
        
        [LHTaskMethod(TASK_DEF_NAME_PROCESS)]
        public Task<string> ProcessOrder(string account_number)
        {
            return Task.FromResult($"Output value: {account_number}");
        }

        [LHTaskMethod("native-arr-param")]
        public Task NativeArrayParam([LHType(masked: false, isLHArray: true)] string[] words)
        {
            return Task.CompletedTask;
        }

        [LHTaskMethod("native-arr-list-param")]
        public Task NativeArrayListParam([LHType(masked: false, isLHArray: true)] List<string> words)
        {
            return Task.CompletedTask;
        }

        [LHTaskMethod("json-arr-param")]
        public Task JsonArrayParam(string[] words)
        {
            return Task.CompletedTask;
        }

        [LHTaskMethod("native-arr-return")]
        [LHType(masked: false, isLHArray: true)]
        public Task<string[]> NativeArrayReturn()
        {
            return Task.FromResult(new string[] { "a" });
        }

        [LHTaskMethod("json-arr-return")]
        public Task<string[]> JsonArrayReturn()
        {
            return Task.FromResult(new string[] { "a" });
        }

        [LHTaskMethod("non-array-lh-array-param")]
        public Task NonArrayLhArrayParam([LHType(masked: false, isLHArray: true)] string word)
        {
            return Task.CompletedTask;
        }

        [LHTaskMethod("non-array-lh-array-return")]
        [LHType(masked: false, isLHArray: true)]
        public Task<string> NonArrayLhArrayReturn()
        {
            return Task.FromResult("word");
        }

        [LHTaskMethod("bytes-lh-array-param")]
        public Task BytesLhArrayParam([LHType(masked: false, isLHArray: true)] byte[] data)
        {
            return Task.CompletedTask;
        }

        [LHTaskMethod("bytes-lh-array-return")]
        [LHType(masked: false, isLHArray: true)]
        public Task<byte[]> BytesLhArrayReturn()
        {
            return Task.FromResult(new byte[] { 1 });
        }

        [LHTaskMethod("invalid-native-arr-jsonobj-param")]
        public Task InvalidNativeArrayJsonObjParam([LHType(masked: false, isLHArray: true)] UnannotatedPoco[] items)
        {
            return Task.CompletedTask;
        }
    }

    // A plain class with no [LHStructDef] — resolves to JSON_OBJ, forbidden in native arrays.
    private class UnannotatedPoco
    {
        public string? Name { get; set; }
    }
}
