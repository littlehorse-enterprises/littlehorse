using System.Collections.Generic;
using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Utils;
using Xunit;

namespace LittleHorse.Sdk.Tests.Utils
{
    public class JsonHandlerTest
    {
        [Fact]
        public void JsonHandler_WithCustomObject_ShouldReturnJsonString()
        {
            var car = new Car {Id = 1, Cost = 134.45E-2f};
            var person = new Person { FirstName = "Test", Age = 35, Cars = new List<Car> { car } };
            
            string result = JsonHandler.ObjectSerializeToJson(person);
            
            string expected = "{\"FirstName\":\"Test\",\"Age\":35,\"Cars\":[{\"Id\":1,\"Cost\":1.3445}]}";
            
            Assert.Equal(expected, result);
        }
        
        [Fact]
        public void JsonHandler_WithJsonString_ShouldReturnCustomObject()
        {
            string jsonString = "{\"FirstName\":\"Test\",\"Age\":35,\"Cars\":[{\"Id\":1,\"Cost\":1.3445}]}";
            
            var result = JsonHandler.DeserializeFromJson(jsonString, typeof(Person));
            
            var actual = (Person) result!;
            
            Assert.Equal("Test", actual.FirstName);
            Assert.Equal(35, actual.Age);
            Assert.Equal(1, actual.Cars![0].Id);
            Assert.Equal(134.45E-2f, actual.Cars[0].Cost);
        }
        
        [Fact]
        public void JsonHandler_WithWrongJsonStringToBeDeserialized_ShouldThrownAnException()
        {
            string jsonString = "wrong json string";
            
            var exception = Assert.Throws<LHJsonProcessingException>(() => 
                JsonHandler.DeserializeFromJson(jsonString, typeof(Car)));
            
            Assert.Contains("Unexpected character encountered while parsing value", exception.Message);
        }
        
        [Fact]
        public void JsonHandler_WithNullValueToBeDeserialized_ShouldThrownAnException()
        {
            string jsonString = null!;
            
            var exception = Assert.Throws<LHJsonProcessingException>(() => 
                JsonHandler.DeserializeFromJson(jsonString, typeof(Car)));
            
            Assert.Contains("Value cannot be null.", exception.Message);
        }
        
        [Fact]
        public void JsonHandler_WithNumberToBeDeserialized_ShouldThrownAnException()
        {
            string jsonString = 5.ToString();
            
            var exception = Assert.Throws<LHJsonProcessingException>(() => 
                JsonHandler.DeserializeFromJson(jsonString, typeof(Car)));
            
            Assert.Contains($"Error converting value {jsonString} to type 'LittleHorse.Sdk.Tests.Car'", 
                exception.Message);
        }
    }
}
