using System;
using LittleHorse.Sdk.Utils;
using Xunit;

namespace LittleHorse.Sdk.Tests.Utils
{
    public class IntegerConverterTest
    {
        [Fact]
        public void ValueConverter_WhenStringIsNull_ShouldReturnZero()
        {
            string value = null!;
            
            var convertedValue = IntegerConverter.FromString(value);
            
            Assert.Equal(0, convertedValue);
        }
        
        [Fact]
        public void ValueConverter_WhenStringIsEmpty_ShouldReturnZero()
        {
            string value = string.Empty;
            
            var convertedValue = IntegerConverter.FromString(value);
            
            Assert.Equal(0, convertedValue);
        }
        
        [Fact]
        public void ValueConverter_WhenStringIsNumber_ShouldReturnAnInteger()
        {
            string value = "75";
            
            var convertedValue = IntegerConverter.FromString(value);
            
            Assert.Equal(int.Parse(value), convertedValue);
        }
        
        [Fact]
        public void ValueConverter_WhenStringIsNotANumber_ShouldReturnFormatException()
        {
            string value = "test-number";
            
            var exception = Assert.Throws<FormatException>(() => IntegerConverter.FromString(value));
            
            Assert.Equal($"Unable to convert '{value}' to an integer.", exception.Message);
        }
    }
}