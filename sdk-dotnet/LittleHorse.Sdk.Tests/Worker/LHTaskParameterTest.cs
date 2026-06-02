using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Threading.Tasks;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Worker;
using Xunit;

namespace LittleHorse.Sdk.Tests.Worker
{
    public class LHTaskParameterTest
    {
        [Fact]
        public void ShouldHandleJsonArrayTaskParameter()
        {
            MethodInfo method = GetTaskMethodByName(typeof(ParameterTestTasks), "test-json-arr");
            ParameterInfo parameter = method.GetParameters()[0];
            var taskParameter = new LHTaskParameter(parameter, 0, logger: null);

            var expectedVariableDef = new VariableDef
            {
                Name = "param1",
                TypeDef = new TypeDefinition { PrimitiveType = VariableType.JsonArr }
            };

            Assert.Equal(expectedVariableDef, taskParameter.VariableDef);
        }

        [Fact]
        public void ShouldHandleNativeArrayTaskParameter()
        {
            MethodInfo method = GetTaskMethodByName(typeof(ParameterTestTasks), "test-native-arr");
            ParameterInfo parameter = method.GetParameters()[0];
            var taskParameter = new LHTaskParameter(parameter, 0, logger: null);

            var expectedVariableDef = new VariableDef
            {
                Name = "param1",
                TypeDef = new TypeDefinition
                {
                    InlineArrayDef = new InlineArrayDef
                    {
                        ArrayType = new TypeDefinition { PrimitiveType = VariableType.Str }
                    }
                }
            };

            Assert.Equal(expectedVariableDef, taskParameter.VariableDef);
        }

        [Fact]
        public void ShouldHandleNativeListTaskParameter()
        {
            MethodInfo method = GetTaskMethodByName(typeof(ParameterTestTasks), "test-native-list");
            ParameterInfo parameter = method.GetParameters()[0];
            var taskParameter = new LHTaskParameter(parameter, 0, logger: null);

            Assert.Equal(TypeDefinition.DefinedTypeOneofCase.InlineArrayDef, taskParameter.VariableDef.TypeDef.DefinedTypeCase);
            Assert.Equal(VariableType.Str, taskParameter.VariableDef.TypeDef.InlineArrayDef.ArrayType.PrimitiveType);
        }

        [Fact]
        public void ShouldFailWhenIsLHArrayUsedOnNonArrayParameter()
        {
            MethodInfo method = GetTaskMethodByName(typeof(ParameterTestTasks), "non-array-lh-array-invalid-task");
            ParameterInfo parameter = method.GetParameters()[0];

            var ex = Assert.Throws<LHTaskSchemaMismatchException>(() => new LHTaskParameter(parameter, 0, logger: null));
            Assert.Contains("@LHType(isLHArray = true) can only be used on array or IList<T> parameters", ex.Message);
        }

        [Fact]
        public void ShouldFailWhenIsLHArrayUsedOnByteArrayParameter()
        {
            MethodInfo method = GetTaskMethodByName(typeof(ParameterTestTasks), "bytes-lh-array-invalid-task");
            ParameterInfo parameter = method.GetParameters()[0];

            var ex = Assert.Throws<LHTaskSchemaMismatchException>(() => new LHTaskParameter(parameter, 0, logger: null));
            Assert.Contains("Cannot use @LHType(isLHArray = true) with byte[]", ex.Message);
        }

        [Fact]
        public void ShouldFailWhenNativeArrayElementResolvesToJsonObj()
        {
            MethodInfo method = GetTaskMethodByName(typeof(ParameterTestTasks), "invalid-native-arr-pojo");
            ParameterInfo parameter = method.GetParameters()[0];

            var ex = Assert.Throws<ArgumentException>(() => new LHTaskParameter(parameter, 0, logger: null));
            Assert.Contains("cannot contain", ex.Message);
        }

        private static MethodInfo GetTaskMethodByName(Type clazz, string taskName)
        {
            return clazz.GetMethods(BindingFlags.Public | BindingFlags.Instance | BindingFlags.DeclaredOnly)
                .Single(m => m.GetCustomAttribute(typeof(LHTaskMethodAttribute)) is LHTaskMethodAttribute attr
                    && attr.Value == taskName);
        }

        private class UnannotatedArrayElement
        {
            public string? Name { get; set; }
        }

        private class ParameterTestTasks
        {
            [LHTaskMethod("test-json-arr")]
            public Task TestJsonArr([LHType(masked: false, name: "param1")] string[] param1)
            {
                return Task.CompletedTask;
            }

            [LHTaskMethod("test-native-arr")]
            public Task TestNativeArr([LHType(masked: false, name: "param1", isLHArray: true)] string[] param1)
            {
                return Task.CompletedTask;
            }

            [LHTaskMethod("test-native-list")]
            public Task TestNativeList([LHType(masked: false, name: "param1", isLHArray: true)] List<string> param1)
            {
                return Task.CompletedTask;
            }

            [LHTaskMethod("non-array-lh-array-invalid-task")]
            public Task NonArrayLhArrayInvalidTask([LHType(masked: false, isLHArray: true)] string customer)
            {
                return Task.CompletedTask;
            }

            [LHTaskMethod("bytes-lh-array-invalid-task")]
            public Task BytesLhArrayInvalidTask([LHType(masked: false, isLHArray: true)] byte[] bytes)
            {
                return Task.CompletedTask;
            }

            [LHTaskMethod("invalid-native-arr-pojo")]
            public Task InvalidNativeArrayPojo([LHType(masked: false, name: "param1", isLHArray: true)] UnannotatedArrayElement[] param1)
            {
                return Task.CompletedTask;
            }
        }
    }
}
