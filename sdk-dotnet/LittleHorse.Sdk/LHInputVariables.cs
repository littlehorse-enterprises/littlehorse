using System.Runtime.CompilerServices;
using LittleHorse.Sdk.Utils;
using Microsoft.Extensions.Configuration;

[assembly: InternalsVisibleTo("LittleHorse.Sdk.Tests")]

namespace LittleHorse.Sdk
{
    internal class LHInputVariables
    {
        private static string GenerateClientId()
        {
            return "client-" + Guid.NewGuid().ToString().Replace("-", "");
        }

        internal string LHC_API_HOST { get; set; } = "localhost";
        internal int LHC_API_PORT { get; set; } = 2023;
        internal string LHC_API_PROTOCOL { get; set; } = "PLAIN";
        internal string LHC_CLIENT_ID { get; set; } = GenerateClientId();
        internal string? LHC_CA_CERT { get; set; }
        internal string? LHC_CLIENT_CERT { get; set; }
        internal string? LHC_CLIENT_KEY { get; set; }
        internal string? LHC_OAUTH_CLIENT_ID { get; set; }
        internal string? LHC_OAUTH_CLIENT_SECRET { get; set; }
        internal string? LHC_OAUTH_ACCESS_TOKEN_URL { get; set; }
        internal int LHW_NUM_WORKER_THREADS { get; set; } = 8;
        internal string LHW_TASK_WORKER_VERSION { get; set; } = string.Empty;


        internal LHInputVariables()
        {
            var apiHost = Environment.GetEnvironmentVariable("LHC_API_HOST");
            if (!string.IsNullOrEmpty(apiHost))
                LHC_API_HOST = apiHost;
            var apiPort = Environment.GetEnvironmentVariable("LHC_API_PORT");
            if (!string.IsNullOrEmpty(apiPort))
                LHC_API_PORT = IntegerConverter.FromString(apiPort);
            var apiProtocol = Environment.GetEnvironmentVariable("LHC_API_PROTOCOL");
            if (!string.IsNullOrEmpty(apiProtocol))
                LHC_API_PROTOCOL = apiProtocol;
            var clientId = Environment.GetEnvironmentVariable("LHC_CLIENT_ID");
            if (!string.IsNullOrEmpty(clientId))
                LHC_CLIENT_ID = clientId;
            var caCert = Environment.GetEnvironmentVariable("LHC_CA_CERT");
            if (!string.IsNullOrEmpty(caCert))
                LHC_CA_CERT = caCert;
            var clientCert = Environment.GetEnvironmentVariable("LHC_CLIENT_CERT");
            if (!string.IsNullOrEmpty(clientCert))
                LHC_CLIENT_CERT = clientCert;
            var clientKey = Environment.GetEnvironmentVariable("LHC_CLIENT_KEY");
            if (!string.IsNullOrEmpty(clientKey))
                LHC_CLIENT_KEY = clientKey;
            var oauthClientId = Environment.GetEnvironmentVariable("LHC_OAUTH_CLIENT_ID");
            if (!string.IsNullOrEmpty(oauthClientId))
                LHC_OAUTH_CLIENT_ID = oauthClientId;
            var oauthClientSecret = Environment.GetEnvironmentVariable("LHC_OAUTH_CLIENT_SECRET");
            if (!string.IsNullOrEmpty(oauthClientSecret))
                LHC_OAUTH_CLIENT_SECRET = oauthClientSecret;
            var oauthAccessTokenUrl = Environment.GetEnvironmentVariable("LHC_OAUTH_ACCESS_TOKEN_URL");
            if (!string.IsNullOrEmpty(oauthAccessTokenUrl))
                LHC_OAUTH_ACCESS_TOKEN_URL = oauthAccessTokenUrl;
            var numberWorkerThreads = Environment.GetEnvironmentVariable("LHW_NUM_WORKER_THREADS");
            if (!string.IsNullOrEmpty(numberWorkerThreads))
                LHW_NUM_WORKER_THREADS = IntegerConverter.FromString(numberWorkerThreads);
            var taskWorkerVersion = Environment.GetEnvironmentVariable("LHW_TASK_WORKER_VERSION");
            if (!string.IsNullOrEmpty(taskWorkerVersion))
                LHW_TASK_WORKER_VERSION = taskWorkerVersion;
        }
        
        internal LHInputVariables(string filePath)
        {
            IConfigurationBuilder builder = new ConfigurationBuilder()
                .AddIniFile(filePath, false, true);
            IConfigurationRoot properties = builder.Build();

            var host = properties["LHC_API_HOST"];
            if (!string.IsNullOrEmpty(host))
                LHC_API_HOST = host;
            var port = properties["LHC_API_PORT"];
            if (!string.IsNullOrEmpty(port))
                LHC_API_PORT = IntegerConverter.FromString(port);
            var apiProtocol = properties[ "LHC_API_PROTOCOL"];
            if (!string.IsNullOrEmpty(apiProtocol))
                LHC_API_PROTOCOL = apiProtocol;
            var clientId = properties["LHC_CLIENT_ID"];
            if (!string.IsNullOrEmpty(clientId))
                LHC_CLIENT_ID = clientId;
            var caCert = properties["LHC_CA_CERT"];
            if (!string.IsNullOrEmpty(caCert))
                LHC_CA_CERT = caCert;
            var clientCert = properties["LHC_CLIENT_CERT"];
            if (!string.IsNullOrEmpty(clientCert))
                LHC_CLIENT_CERT = clientCert;
            var clientKey = properties["LHC_CLIENT_KEY"];
            if (!string.IsNullOrEmpty(clientKey))
                LHC_CLIENT_KEY = clientKey;
            var oauthClientId = properties["LHC_OAUTH_CLIENT_ID"];
            if (!string.IsNullOrEmpty(oauthClientId))
                LHC_OAUTH_CLIENT_ID = oauthClientId;
            var oauthClientSecret = properties["LHC_OAUTH_CLIENT_SECRET"];
            if (!string.IsNullOrEmpty(oauthClientSecret))
                LHC_OAUTH_CLIENT_SECRET = oauthClientSecret;
            var oauthAccessTokenUrl = properties["LHC_OAUTH_ACCESS_TOKEN_URL"];
            if (!string.IsNullOrEmpty(oauthAccessTokenUrl))
                LHC_OAUTH_ACCESS_TOKEN_URL = oauthAccessTokenUrl;
            var numberWorkerThreads = properties["LHW_NUM_WORKER_THREADS"];
            if (!string.IsNullOrEmpty(numberWorkerThreads))
                LHW_NUM_WORKER_THREADS = IntegerConverter.FromString(numberWorkerThreads);
            var taskWorkerVersion = properties["LHW_TASK_WORKER_VERSION"];
            if (!string.IsNullOrEmpty(taskWorkerVersion))
                LHW_TASK_WORKER_VERSION = taskWorkerVersion;
        }
    }
}
