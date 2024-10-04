using System;
using System.Collections.Generic;
using System.IO;
using Xunit;

namespace LittleHorse.Sdk.Tests.Internal
{
    public class LHInputVariablesTest
    {
        private void CleanEnvironmentVariables()
        {
            Environment.SetEnvironmentVariable("LHC_API_HOST", null);
            Environment.SetEnvironmentVariable("LHC_API_PORT", null);
            Environment.SetEnvironmentVariable("LHC_API_PROTOCOL", null);
            Environment.SetEnvironmentVariable("LHC_CLIENT_ID", null);
            Environment.SetEnvironmentVariable("LHC_CA_CERT", null);
            Environment.SetEnvironmentVariable("LHC_CLIENT_CERT", null);
            Environment.SetEnvironmentVariable("LHC_CLIENT_KEY", null);
            Environment.SetEnvironmentVariable("LHC_OAUTH_CLIENT_ID", null);
            Environment.SetEnvironmentVariable("LHC_OAUTH_CLIENT_SECRET", null);
            Environment.SetEnvironmentVariable("LHC_OAUTH_ACCESS_TOKEN_URL", null);
            Environment.SetEnvironmentVariable("LHW_NUM_WORKER_THREADS", null);
            Environment.SetEnvironmentVariable("LHW_TASK_WORKER_VERSION", null);
        }

        [Fact]
        public void LHConfigVariables_WithLHEnvVariables_ShouldReturnSetOptions()
        {
            string host = "host";
            string port = "254";
            string protocol = "TLS";
            string clientId = "client-id";
            string caCert = "caCert";
            string clientCert = "cert";
            string clientKey = "client-key";
            string oauthClientId = "oauth-client-id";
            string oauthClientSecret = "oauth-client-secret";
            string oauthAccessTokenUrl = "oauth-access-token-url";
            string numberWorkerThreads = "15";
            string taskWorkerVersion = "task-worker-version";
            
            Environment.SetEnvironmentVariable("LHC_API_HOST", host);
            Environment.SetEnvironmentVariable("LHC_API_PORT", port);
            Environment.SetEnvironmentVariable("LHC_API_PROTOCOL", protocol);
            Environment.SetEnvironmentVariable("LHC_CLIENT_ID", clientId);
            Environment.SetEnvironmentVariable("LHC_CA_CERT", caCert);
            Environment.SetEnvironmentVariable("LHC_CLIENT_CERT", clientCert);
            Environment.SetEnvironmentVariable("LHC_CLIENT_KEY", clientKey);
            Environment.SetEnvironmentVariable("LHC_OAUTH_CLIENT_ID", oauthClientId);
            Environment.SetEnvironmentVariable("LHC_OAUTH_CLIENT_SECRET", oauthClientSecret);
            Environment.SetEnvironmentVariable("LHC_OAUTH_ACCESS_TOKEN_URL", oauthAccessTokenUrl);
            Environment.SetEnvironmentVariable("LHW_NUM_WORKER_THREADS", numberWorkerThreads);
            Environment.SetEnvironmentVariable("LHW_TASK_WORKER_VERSION", taskWorkerVersion);

            var inputVariables = new LHInputVariables();
            
            Assert.Equal(host, inputVariables.LHC_API_HOST);
            Assert.Equal(int.Parse(port), inputVariables.LHC_API_PORT);
            Assert.Equal(protocol, inputVariables.LHC_API_PROTOCOL);
            Assert.Equal(clientId, inputVariables.LHC_CLIENT_ID);
            Assert.Equal(caCert, inputVariables.LHC_CA_CERT);
            Assert.Equal(clientCert, inputVariables.LHC_CLIENT_CERT);
            Assert.Equal(clientKey, inputVariables.LHC_CLIENT_KEY);
            Assert.Equal(oauthClientId, inputVariables.LHC_OAUTH_CLIENT_ID);
            Assert.Equal(oauthClientSecret, inputVariables.LHC_OAUTH_CLIENT_SECRET);
            Assert.Equal(oauthAccessTokenUrl, inputVariables.LHC_OAUTH_ACCESS_TOKEN_URL);
            Assert.Equal(int.Parse(numberWorkerThreads), inputVariables.LHW_NUM_WORKER_THREADS);
            Assert.Equal(taskWorkerVersion, inputVariables.LHW_TASK_WORKER_VERSION);
            
            CleanEnvironmentVariables();
        }
        
        [Fact]
        public void LHConfigVariables_WithoutLHEnvVariables_ShouldReturnDefaultOptions()
        {
            var inputVariables = new LHInputVariables();

            Assert.Equal("localhost", inputVariables.LHC_API_HOST);
            Assert.Equal(2023, inputVariables.LHC_API_PORT);
            Assert.Equal("PLAIN", inputVariables.LHC_API_PROTOCOL);
            Assert.StartsWith("client-", inputVariables.LHC_CLIENT_ID);
            Assert.Equal(8, inputVariables.LHW_NUM_WORKER_THREADS);
            Assert.Equal(string.Empty, inputVariables.LHW_TASK_WORKER_VERSION);
        }

        [Fact]
        public void LHConfigVariables_WithLHOptionsInFile_ShouldReturnSetOptions()
        {
            const string lhConfigFileName = "littlehorse.config";
            string inputVariablesFilePath = TestUtils.BuildFilePath(lhConfigFileName);
            var keyValueLHConfigs = new Dictionary<string, string>
            {
                { "LHC_API_HOST", "host-test" },
                { "LHC_API_PORT", "111" },
                { "LHC_API_PROTOCOL", "TLS" },
                { "LHC_CA_CERT", "ca_file_path" },
                { "LHW_TASK_WORKER_VERSION", "test" }
            };
            TestUtils.WriteContentInFile(keyValueLHConfigs, inputVariablesFilePath);
            
            var inputVariables = new LHInputVariables(inputVariablesFilePath);
            
            Assert.Equal(keyValueLHConfigs["LHC_API_HOST"], inputVariables.LHC_API_HOST);
            Assert.Equal(int.Parse(keyValueLHConfigs["LHC_API_PORT"]), inputVariables.LHC_API_PORT);
            Assert.Equal(keyValueLHConfigs["LHC_API_PROTOCOL"], inputVariables.LHC_API_PROTOCOL);
            Assert.Equal(keyValueLHConfigs["LHC_CA_CERT"], inputVariables.LHC_CA_CERT);
            Assert.Equal(keyValueLHConfigs["LHW_TASK_WORKER_VERSION"], inputVariables.LHW_TASK_WORKER_VERSION);
        }
        
        [Fact]
        public void LHConfigVariables_WithNoLHOptionsInFile_ShouldReturnDefaultOptions()
        {
            const string lhConfigFileName = "littlehorse.config";
            string inputVariablesFilePath = TestUtils.BuildFilePath(lhConfigFileName);
            var keyValueLHConfigs = new Dictionary<string, string>
            {
                { "API_HOST", "host" },
                { "API_PORT", "111" },
                { "API_PROTOCOL", "TLS" },
                { "CA_CERT", "ca-path" }
            };
            TestUtils.WriteContentInFile(keyValueLHConfigs, inputVariablesFilePath);
            
            var inputVariables = new LHInputVariables(inputVariablesFilePath);
            
            Assert.Equal(DefaultLHConfigVariables.LHC_API_HOST, inputVariables.LHC_API_HOST);
            Assert.Equal(DefaultLHConfigVariables.LHC_API_PORT, inputVariables.LHC_API_PORT);
            Assert.Equal(DefaultLHConfigVariables.LHC_API_PROTOCOL, inputVariables.LHC_API_PROTOCOL);
            Assert.StartsWith(DefaultLHConfigVariables.LHC_CLIENT_ID, inputVariables.LHC_CLIENT_ID);
            Assert.Equal(DefaultLHConfigVariables.LHW_NUM_WORKER_THREADS, inputVariables.LHW_NUM_WORKER_THREADS);
            Assert.Equal(DefaultLHConfigVariables.LHW_TASK_WORKER_VERSION, inputVariables.LHW_TASK_WORKER_VERSION);
        }
        
        [Fact]
        public void LHConfigVariables_WithEmptyLHFile_ShouldReturnDefaultOptions()
        {
            const string lhConfigFileName = "littlehorse.config";
            string inputVariablesFilePath = TestUtils.BuildFilePath(lhConfigFileName);
            var keyValueLHConfigs = new Dictionary<string, string>();
            TestUtils.WriteContentInFile(keyValueLHConfigs, inputVariablesFilePath);
            
            var inputVariables = new LHInputVariables(inputVariablesFilePath);
            
            Assert.Equal(DefaultLHConfigVariables.LHC_API_HOST, inputVariables.LHC_API_HOST);
            Assert.Equal(DefaultLHConfigVariables.LHC_API_PORT, inputVariables.LHC_API_PORT);
            Assert.Equal(DefaultLHConfigVariables.LHC_API_PROTOCOL, inputVariables.LHC_API_PROTOCOL);
            Assert.StartsWith(DefaultLHConfigVariables.LHC_CLIENT_ID, inputVariables.LHC_CLIENT_ID);
            Assert.Equal(DefaultLHConfigVariables.LHW_NUM_WORKER_THREADS, inputVariables.LHW_NUM_WORKER_THREADS);
            Assert.Equal(DefaultLHConfigVariables.LHW_TASK_WORKER_VERSION, inputVariables.LHW_TASK_WORKER_VERSION);
        }
        
        [Fact]
        public void LHConfigVariables_WithoutLHFile_ShouldThrowException()
        {
            const string inputVariablesFileName = "not_found_littlehorse.config";
            
            var exception = Assert.Throws<FileNotFoundException>(() => new LHInputVariables(inputVariablesFileName));

            Assert.Contains($"File {inputVariablesFileName} does not exist.", exception.Message);
        }
        
        [Fact]
        public void LHConfigVariables_WithSomeLHOptionsCommentedInFile_ShouldReturnSetOptions()
        {
            const string lhConfigFileName = "littlehorse.config";
            string inputVariablesFilePath = TestUtils.BuildFilePath(lhConfigFileName);
            var keyValueLHConfigs = new Dictionary<string, string>
            {
                { "LHC_API_HOST", "host" },
                { "LHC_API_PORT", "111" },
                { "#LHC_API_PROTOCOL", "TLS" },
                { "#LHC_CA_CERT", "ca-path" }
            };
            TestUtils.WriteContentInFile(keyValueLHConfigs, inputVariablesFilePath);
            
            var inputVariables = new LHInputVariables(inputVariablesFilePath);
            
            Assert.Equal(keyValueLHConfigs["LHC_API_HOST"], inputVariables.LHC_API_HOST);
            Assert.Equal(int.Parse(keyValueLHConfigs["LHC_API_PORT"]), inputVariables.LHC_API_PORT);
            Assert.Equal(DefaultLHConfigVariables.LHC_API_PROTOCOL, inputVariables.LHC_API_PROTOCOL);
            Assert.Null(inputVariables.LHC_CA_CERT);
            Assert.Equal(string.Empty, inputVariables.LHW_TASK_WORKER_VERSION);
        }
        
        [Fact]
        public void LHConfigVariables_WithAllLHOptionsCommentedInFile_ShouldReturnDefaultOptions()
        {
            const string lhConfigFileName = "littlehorse.config";
            string inputVariablesFilePath = TestUtils.BuildFilePath(lhConfigFileName);
            var keyValueLHConfigs = new Dictionary<string, string>
            {
                { "#LHC_API_HOST", "host" },
                { "#LHC_API_PORT", "111" },
                { "#LHC_API_PROTOCOL", "TLS" },
                { "#LHC_CLIENT_ID", "TEST_CLIENT-ID" }
            };
            TestUtils.WriteContentInFile(keyValueLHConfigs, inputVariablesFilePath);
            
            var inputVariables = new LHInputVariables(inputVariablesFilePath);
            
            Assert.Equal(DefaultLHConfigVariables.LHC_API_HOST, inputVariables.LHC_API_HOST);
            Assert.Equal(DefaultLHConfigVariables.LHC_API_PORT, inputVariables.LHC_API_PORT);
            Assert.Equal(DefaultLHConfigVariables.LHC_API_PROTOCOL, inputVariables.LHC_API_PROTOCOL);
            Assert.StartsWith(DefaultLHConfigVariables.LHC_CLIENT_ID, inputVariables.LHC_CLIENT_ID);
            Assert.Equal(DefaultLHConfigVariables.LHW_NUM_WORKER_THREADS, inputVariables.LHW_NUM_WORKER_THREADS);
            Assert.Equal(DefaultLHConfigVariables.LHW_TASK_WORKER_VERSION, inputVariables.LHW_TASK_WORKER_VERSION);
        }
    }
}
