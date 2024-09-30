using System;
using LittleHorse.Sdk.Internal;
using Xunit;

namespace LittleHorse.Sdk.Tests.Internal
{
    public class LHOptionsBinderTest
    { 
        [Fact]
        public void OptionsBinder_WithLHEnvVariables_ShouldReturnSetOptions()
        {
            string host = "host";
            string port = "254";
            string protocol = "TLS";
            string clientId = "client-id";
            string clientCert = "cert";
            string clientKey = "client-key";
            string oauthClientId = "oauth-client-id";
            string oauthClientSecret = "oauth-client-secret";
            string oauthAccessTokenUrl = "oauth-access-token-url";
            string serverConnectListener = "server-connect-listener";
            string numberWorkerThreads = "15";
            string taskWorkerVersion = "task-worker-version";
            
            Environment.SetEnvironmentVariable("LHC_API_HOST", host);
            Environment.SetEnvironmentVariable("LHC_API_PORT", port);
            Environment.SetEnvironmentVariable("LHC_API_PROTOCOL", protocol);
            Environment.SetEnvironmentVariable("LHC_CLIENT_ID", clientId);
            Environment.SetEnvironmentVariable("LHC_CLIENT_CERT", clientCert);
            Environment.SetEnvironmentVariable("LHC_CLIENT_KEY", clientKey);
            Environment.SetEnvironmentVariable("LHC_OAUTH_CLIENT_ID", oauthClientId);
            Environment.SetEnvironmentVariable("LHC_OAUTH_CLIENT_SECRET", oauthClientSecret);
            Environment.SetEnvironmentVariable("LHC_OAUTH_ACCESS_TOKEN_URL", oauthAccessTokenUrl);
            Environment.SetEnvironmentVariable("LHW_SERVER_CONNECT_LISTENER", serverConnectListener);
            Environment.SetEnvironmentVariable("LHW_NUM_WORKER_THREADS", numberWorkerThreads);
            Environment.SetEnvironmentVariable("LHW_TASK_WORKER_VERSION", taskWorkerVersion);
            
            var options = LHOptionsBinder.GetOptionsFromEnvironmentVariables();
            
            Assert.Equal(host, options.LHC_API_HOST);
            Assert.Equal(int.Parse(port), options.LHC_API_PORT);
            Assert.Equal(protocol, options.LHC_API_PROTOCOL);
            Assert.Equal(clientId, options.LHC_CLIENT_ID);
            Assert.Equal(clientCert, options.LHC_CLIENT_CERT);
            Assert.Equal(clientKey, options.LHC_CLIENT_KEY);
            Assert.Equal(oauthClientId, options.LHC_OAUTH_CLIENT_ID);
            Assert.Equal(oauthClientSecret, options.LHC_OAUTH_CLIENT_SECRET);
            Assert.Equal(oauthAccessTokenUrl, options.LHC_OAUTH_ACCESS_TOKEN_URL);
            Assert.Equal(serverConnectListener, options.LHW_SERVER_CONNECT_LISTENER);
            Assert.Equal(int.Parse(numberWorkerThreads), options.LHW_NUM_WORKER_THREADS);
            Assert.Equal(taskWorkerVersion, options.LHW_TASK_WORKER_VERSION);
        }
        
        [Fact]
        public void OptionsBinder_WithoutLHEnvVariables_ShouldReturnDefaultOptions()
        {
            var options = LHOptionsBinder.GetOptionsFromEnvironmentVariables();

            Assert.Equal("localhost", options.LHC_API_HOST);
            Assert.Equal(2023, options.LHC_API_PORT);
            Assert.Equal("PLAIN", options.LHC_API_PROTOCOL);
            Assert.StartsWith("client-", options.LHC_CLIENT_ID);
            Assert.Equal("PLAIN", options.LHW_SERVER_CONNECT_LISTENER);
            Assert.Equal(8, options.LHW_NUM_WORKER_THREADS);
            Assert.Equal(string.Empty, options.LHW_TASK_WORKER_VERSION);
        }
    }
}
