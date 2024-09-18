using LittleHorse.Sdk.Exceptions;
using LittleHorse.Sdk.Internal;
using Microsoft.Extensions.Logging;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Internal
{
    public class LHLoggerFactoryProviderTest
    {
        [Fact]
        public void Initialize_WithCustomLoggerFactory_ShouldCreateLogger()
        {
            var mockLoggerFactory = new Mock<ILoggerFactory>();
            
            LHLoggerFactoryProvider.Initialize(mockLoggerFactory.Object);
            var logger = LHLoggerFactoryProvider.GetLogger<string>();
            
            Assert.NotNull(logger);
            Assert.IsAssignableFrom<ILogger<string>>(logger);
        }
        
        [Fact]
        public void Initialize_WithoutLoggerFactory_ShouldAVoidWritingLogs()
        {
            LHLoggerFactoryProvider.Initialize();
            var logger = LHLoggerFactoryProvider.GetLogger<string>();
            logger.LogInformation("This log should not be logged");
            
            Assert.NotNull(logger);
        }
        
        [Fact]
        public void GetLogger_WithoutInitialize_ShouldThrowLHLoggerFactoryNullException()
        {
            var exception = Assert.Throws<LHLoggerFactoryNullException>(() => LHLoggerFactoryProvider.GetLogger<string>());
            
            Assert.Equal("_loggerFactory does not have a valid value and it is trying to create a logger instance.", exception.Message);
        }
    }
}
