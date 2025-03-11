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
        internal string? LHC_TENANT_ID { get; set; }
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
            var tenantId = Environment.GetEnvironmentVariable("LHC_TENANT_ID");
            if (!string.IsNullOrEmpty(tenantId))
                LHC_TENANT_ID = tenantId;
        }
        
        internal LHInputVariables(string filePath)
        {
            IConfigurationBuilder builder = new ConfigurationBuilder()
                .AddIniFile(filePath, false, false);
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
            var tenantId = properties["LHC_TENANT_ID"];
            if (!string.IsNullOrEmpty(tenantId))
                LHC_TENANT_ID = tenantId;
        }

        internal LHInputVariables(Dictionary<string, string> configArguments)
        {
            var host = GetValueIfKeyIsPresent(configArguments!, "LHC_API_HOST");
            if (!string.IsNullOrEmpty(host))
                LHC_API_HOST = host;
            var port = GetValueIfKeyIsPresent(configArguments!, "LHC_API_PORT");
            if (!string.IsNullOrEmpty(port))
                LHC_API_PORT = IntegerConverter.FromString(port);
            var apiProtocol = GetValueIfKeyIsPresent(configArguments!,  "LHC_API_PROTOCOL");
            if (!string.IsNullOrEmpty(apiProtocol))
                LHC_API_PROTOCOL = apiProtocol;
            var clientId = GetValueIfKeyIsPresent(configArguments!, "LHC_CLIENT_ID");
            if (!string.IsNullOrEmpty(clientId))
                LHC_CLIENT_ID = clientId;
            var caCert = GetValueIfKeyIsPresent(configArguments!, "LHC_CA_CERT");
            if (!string.IsNullOrEmpty(caCert))
                LHC_CA_CERT = caCert;
            var clientCert = GetValueIfKeyIsPresent(configArguments!, "LHC_CLIENT_CERT");
            if (!string.IsNullOrEmpty(clientCert))
                LHC_CLIENT_CERT = clientCert;
            var clientKey = GetValueIfKeyIsPresent(configArguments!, "LHC_CLIENT_KEY");
            if (!string.IsNullOrEmpty(clientKey))
                LHC_CLIENT_KEY = clientKey;
            var oauthClientId = GetValueIfKeyIsPresent(configArguments!, "LHC_OAUTH_CLIENT_ID");
            if (!string.IsNullOrEmpty(oauthClientId))
                LHC_OAUTH_CLIENT_ID = oauthClientId;
            var oauthClientSecret = GetValueIfKeyIsPresent(configArguments!, "LHC_OAUTH_CLIENT_SECRET");
            if (!string.IsNullOrEmpty(oauthClientSecret))
                LHC_OAUTH_CLIENT_SECRET = oauthClientSecret;
            var oauthAccessTokenUrl = GetValueIfKeyIsPresent(
                configArguments!, "LHC_OAUTH_ACCESS_TOKEN_URL");
            if (!string.IsNullOrEmpty(oauthAccessTokenUrl))
                LHC_OAUTH_ACCESS_TOKEN_URL = oauthAccessTokenUrl;
            var numberWorkerThreads = GetValueIfKeyIsPresent(configArguments!, "LHW_NUM_WORKER_THREADS");
            if (!string.IsNullOrEmpty(numberWorkerThreads))
                LHW_NUM_WORKER_THREADS = IntegerConverter.FromString(numberWorkerThreads);
            var taskWorkerVersion = GetValueIfKeyIsPresent(configArguments!, "LHW_TASK_WORKER_VERSION");
            if (!string.IsNullOrEmpty(taskWorkerVersion))
                LHW_TASK_WORKER_VERSION = taskWorkerVersion;
            var tenantId = GetValueIfKeyIsPresent(configArguments!, "LHC_TENANT_ID");
            if (!string.IsNullOrEmpty(tenantId))
                LHC_TENANT_ID = tenantId;
        }

        private string GetValueIfKeyIsPresent(Dictionary<string, string?> pairInFile, string keyName)
        {
            var tryGetValue = pairInFile.TryGetValue(keyName, out var value);
            if (tryGetValue)
                return value!;

            return string.Empty;
        }
    }
}
