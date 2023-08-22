using LittleHorse.Common.Configuration.Extension;
using LittleHorse.Common.Configuration.Implementations;
using Microsoft.Extensions.Configuration;
using Moq;

namespace LittleHorse.Test.Common.Configuration
{
    public class LHWorkerConfigTest
    {
        [Fact]
        public void NoConfiguration_ReturnDefaultValues()
        {
            string defaultHost = "localhost";
            int defaultPort = 2023;
            string defaultConnectListener = "PLAIN";
            int defaultWorkerThreads = 8;

            var configuration = new Mock<IConfiguration>();

            var workerConfig = new LHWorkerConfig(configuration.Object);

            Assert.Equal(defaultHost, workerConfig.APIBootstrapHost);
            Assert.Equal(defaultPort, workerConfig.APIBootstrapPort);
            Assert.NotEmpty(workerConfig.ClientId);
            Assert.Equal(defaultConnectListener, workerConfig.ConnectListener);
            Assert.Equal(string.Empty, workerConfig.TaskWorkerVersion);
            Assert.Equal(defaultWorkerThreads, workerConfig.WorkerThreads);
        }

        [Fact]
        public void NoConfiguration_NoOAuth()
        {
            var configuration = new Mock<IConfiguration>();

            var workerConfig = new LHWorkerConfig(configuration.Object);

            Assert.False(workerConfig.IsOAuth);
        }

        [Fact]
        public void AddOAuthConfiguration_IsOAuth()
        {
            var configuration = new ConfigurationBuilder().AddLHWorkerConfiguration(new Dictionary<string, string?>()
            {
                ["LHC_OAUTH_CLIENT_ID"] = "DUMMY_CLIENT_ID",
                ["LHC_OAUTH_CLIENT_SECRET"] = "DUMMY_CLIENT_SECRET",
                ["LHC_OAUTH_AUTHORIZATION_SERVER"] = "dummyserver.com"
            }).Build();

            var workerConfig = new LHWorkerConfig(configuration);

            Assert.True(workerConfig.IsOAuth);
        }

        [Fact]
        public void GetClientWithSameChannel_OneChannelCreated()
        {
            var configuration = new Mock<IConfiguration>();

            var workerConfig = new LHWorkerConfig(configuration.Object);

            workerConfig.GetGrcpClientInstance();
            workerConfig.GetGrcpClientInstance();

            Assert.Equal(1, workerConfig.ActiveChannelsCount);
        }

        [Fact]
        public void GetClientWithDifferentChannel_TwoChannelCreated()
        {
            var configuration = new Mock<IConfiguration>();

            var workerConfig = new LHWorkerConfig(configuration.Object);

            workerConfig.GetGrcpClientInstance("localhost", 2023);
            workerConfig.GetGrcpClientInstance("localhost", 2025);

            Assert.Equal(2, workerConfig.ActiveChannelsCount);
        }

        [Fact]
        public void SetEnvironmentVariables_VariablesAsConfiguration()
        {
            var LHC_API_HOST = "testhost";
            var LHC_API_PORT = 2020;
            var LHC_CLIENT_ID = "client-" + Guid.NewGuid().ToString().Replace("-", "");
            var LHC_OAUTH_CLIENT_ID = "test_oauth_client_id";
            var LHC_OAUTH_CLIENT_SECRET = "test-oauth_client_secret";
            var LHC_OAUTH_AUTHORIZATION_SERVER = "test-server:2023";
            var LHW_SERVER_CONNECT_LISTENER = "OTHER";
            var LHW_NUM_WORKER_THREADS = 4;
            var LHW_TASK_WORKER_VERSION = "v1.0.2";

            Environment.SetEnvironmentVariable("LHC_API_HOST", LHC_API_HOST);
            Environment.SetEnvironmentVariable("LHC_API_PORT", LHC_API_PORT.ToString());
            Environment.SetEnvironmentVariable("LHC_CLIENT_ID", LHC_CLIENT_ID);
            Environment.SetEnvironmentVariable("LHC_OAUTH_CLIENT_ID", LHC_OAUTH_CLIENT_ID);
            Environment.SetEnvironmentVariable("LHC_OAUTH_CLIENT_SECRET", LHC_OAUTH_CLIENT_SECRET);
            Environment.SetEnvironmentVariable("LHC_OAUTH_AUTHORIZATION_SERVER", LHC_OAUTH_AUTHORIZATION_SERVER);
            Environment.SetEnvironmentVariable("LHW_SERVER_CONNECT_LISTENER", LHW_SERVER_CONNECT_LISTENER);
            Environment.SetEnvironmentVariable("LHW_NUM_WORKER_THREADS", LHW_NUM_WORKER_THREADS.ToString());
            Environment.SetEnvironmentVariable("LHW_TASK_WORKER_VERSION", LHW_TASK_WORKER_VERSION);

            var variables = Environment.GetEnvironmentVariables();

            var configuration = new ConfigurationBuilder().AddLHWorkerConfiguration().Build();

            var workerConfig = new LHWorkerConfig(configuration);

            Assert.Equal(LHC_API_HOST, workerConfig.APIBootstrapHost);
            Assert.Equal(LHC_API_PORT, workerConfig.APIBootstrapPort);
            Assert.Equal(LHC_CLIENT_ID, workerConfig.ClientId);
            Assert.True(workerConfig.IsOAuth);
            Assert.Equal(LHW_SERVER_CONNECT_LISTENER, workerConfig.ConnectListener);
            Assert.Equal(LHW_NUM_WORKER_THREADS, workerConfig.WorkerThreads);
            Assert.Equal(LHW_TASK_WORKER_VERSION, workerConfig.TaskWorkerVersion);
        }
    }
}