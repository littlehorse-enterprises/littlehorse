using System;
using System.Linq;
using System.Reflection;
using System.Threading.Tasks;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Worker;
using Xunit;

namespace LittleHorse.Sdk.Tests.Worker
{
    public class LHTaskReturnTypeTest
    {
        [Fact]
        public void ShouldHandleJsonArrayTaskReturnType()
        {
            MethodInfo taskMethod = GetTaskMethodByName(typeof(ReturnTypeTestTasks), "test-json-arr");
            var taskReturnType = new LHTaskReturnType(taskMethod);

            var expectedReturnType = new ReturnType
            {
                ReturnType_ = new TypeDefinition { PrimitiveType = VariableType.JsonArr }
            };

            Assert.Equal(expectedReturnType, taskReturnType.ReturnType);
        }

        [Fact]
        public void ShouldHandleNativeArrayTaskReturnType()
        {
            MethodInfo taskMethod = GetTaskMethodByName(typeof(ReturnTypeTestTasks), "test-native-arr");
            var taskReturnType = new LHTaskReturnType(taskMethod);

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

            Assert.Equal(expectedReturnType, taskReturnType.ReturnType);
        }

        [Fact]
        public void ShouldFailWhenIsLHArrayUsedOnNonArrayReturnType()
        {
            MethodInfo taskMethod = GetTaskMethodByName(typeof(ReturnTypeTestTasks), "non-array-lh-array-invalid-task");

            var ex = Assert.Throws<LHTaskSchemaMismatchException>(() => new LHTaskReturnType(taskMethod));
            Assert.Contains("@LHType(isLHArray = true) can only be used on array or IList<T> return types", ex.Message);
        }

        [Fact]
        public void ShouldFailWhenIsLHArrayUsedOnByteArrayReturnType()
        {
            MethodInfo taskMethod = GetTaskMethodByName(typeof(ReturnTypeTestTasks), "bytes-lh-array-invalid-task");

            var ex = Assert.Throws<LHTaskSchemaMismatchException>(() => new LHTaskReturnType(taskMethod));
            Assert.Contains("Cannot use @LHType(isLHArray = true) with byte[]", ex.Message);
        }

        [Fact]
        public void ShouldFailWhenNativeArrayReturnElementResolvesToJsonObj()
        {
            MethodInfo taskMethod = GetTaskMethodByName(typeof(ReturnTypeTestTasks), "invalid-native-arr-pojo");

            var ex = Assert.Throws<ArgumentException>(() => new LHTaskReturnType(taskMethod));
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

        private class ReturnTypeTestTasks
        {
            [LHTaskMethod("test-json-arr")]
            public Task<string[]> TestJsonArr()
            {
                return Task.FromResult(new string[] { "a" });
            }

            [LHTaskMethod("test-native-arr")]
            [LHType(masked: false, isLHArray: true)]
            public Task<string[]> TestNativeArr()
            {
                return Task.FromResult(new string[] { "a" });
            }

            [LHTaskMethod("non-array-lh-array-invalid-task")]
            [LHType(masked: false, isLHArray: true)]
            public Task<string> NonArrayLhArrayInvalidTask()
            {
                return Task.FromResult("hello");
            }

            [LHTaskMethod("bytes-lh-array-invalid-task")]
            [LHType(masked: false, isLHArray: true)]
            public Task<byte[]> BytesLhArrayInvalidTask()
            {
                return Task.FromResult(new byte[] { 1, 2, 3 });
            }

            [LHTaskMethod("invalid-native-arr-pojo")]
            [LHType(masked: false, isLHArray: true)]
            public Task<UnannotatedArrayElement[]> InvalidNativeArrayPojo()
            {
                return Task.FromResult(new[] { new UnannotatedArrayElement() });
            }
        }
    }
}
