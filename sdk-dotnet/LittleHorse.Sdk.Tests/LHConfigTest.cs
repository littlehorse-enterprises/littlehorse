using System;
using System.Threading;
using Xunit;

namespace LittleHorse.Sdk.Tests
{
    public class LHConfigTest
    {
        [Fact]
        public void LHConfig_WhenInitializedWithoutParams_ShouldReadFromEnvironmentVariables()
        {
            Thread.Sleep(5);
            string host = "test-host";
            string port = "2523";
            Environment.SetEnvironmentVariable("LHC_API_HOST", host);
            Environment.SetEnvironmentVariable("LHC_API_PORT", port);
            
            var config = new LHConfig();

            Assert.Equal(host, config.BootstrapHost);
            Assert.Equal(int.Parse(port), config.BootstrapPort);
        }
        
        [Fact]
        public void LHConfig_WhenPathIsProvided_ShouldLoadVariablesFromFile()
        {
            
        }
        
        [Fact]
        public void LHConfig_WhenDictionaryIsProvided_ShouldLoadVariablesFromDictionary()
        {
            
        }
    }
}