using System.Runtime.CompilerServices;
using LittleHorse.Common.Configuration.Models;
using LittleHorse.Sdk.Utils;

[assembly: InternalsVisibleTo("LittleHorse.Sdk.Tests")]

namespace LittleHorse.Sdk.Internal {
    internal static class LHOptionsBinder {
        private static bool _tryGetValue;

        internal static LHOptions GetOptionsFromEnvironmentVariables()
        {
            var options = new LHOptions();
            
            var apiHost = Environment.GetEnvironmentVariable("LHC_API_HOST");
            if (!string.IsNullOrEmpty(apiHost))
                options.LHC_API_HOST = apiHost;
            var apiPort = Environment.GetEnvironmentVariable("LHC_API_PORT");
            if (!string.IsNullOrEmpty(apiPort))
                options.LHC_API_PORT = IntegerConverter.FromString(apiPort);
            var apiProtocol = Environment.GetEnvironmentVariable("LHC_API_PROTOCOL");
            if (!string.IsNullOrEmpty(apiProtocol))
                options.LHC_API_PROTOCOL = apiProtocol;
            var clientId = Environment.GetEnvironmentVariable("LHC_CLIENT_ID");
            if (!string.IsNullOrEmpty(clientId))
                options.LHC_CLIENT_ID = clientId;
            var caCert = Environment.GetEnvironmentVariable("LHC_CA_CERT");
            if (!string.IsNullOrEmpty(caCert))
                options.LHC_CA_CERT = caCert;
            var clientCert = Environment.GetEnvironmentVariable("LHC_CLIENT_CERT");
            if (!string.IsNullOrEmpty(clientCert))
                options.LHC_CLIENT_CERT = clientCert;
            var clientKey = Environment.GetEnvironmentVariable("LHC_CLIENT_KEY");
            if (!string.IsNullOrEmpty(clientKey))
                options.LHC_CLIENT_KEY = clientKey;
            var oauthClientId = Environment.GetEnvironmentVariable("LHC_OAUTH_CLIENT_ID");
            if (!string.IsNullOrEmpty(oauthClientId))
                options.LHC_OAUTH_CLIENT_ID = oauthClientId;
            var oauthClientSecret = Environment.GetEnvironmentVariable("LHC_OAUTH_CLIENT_SECRET");
            if (!string.IsNullOrEmpty(oauthClientSecret))
                options.LHC_OAUTH_CLIENT_SECRET = oauthClientSecret;
            var oauthAccessTokenUrl = Environment.GetEnvironmentVariable("LHC_OAUTH_ACCESS_TOKEN_URL");
            if (!string.IsNullOrEmpty(oauthAccessTokenUrl))
                options.LHC_OAUTH_ACCESS_TOKEN_URL = oauthAccessTokenUrl;
            var numberWorkerThreads = Environment.GetEnvironmentVariable("LHW_NUM_WORKER_THREADS");
            if (!string.IsNullOrEmpty(numberWorkerThreads))
                options.LHW_NUM_WORKER_THREADS = IntegerConverter.FromString(numberWorkerThreads);
            var taskWorkerVersion = Environment.GetEnvironmentVariable("LHW_TASK_WORKER_VERSION");
            if (!string.IsNullOrEmpty(taskWorkerVersion))
                options.LHW_TASK_WORKER_VERSION = taskWorkerVersion;

            return options;
        }

        internal static LHOptions GetOptionsFromFile(string filePath)
        {
            var options = new LHOptions();

            Dictionary<string, string> lHOptionsInFile = FileManager.GetDictFromPath(filePath);

            var host = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_API_HOST");
            if (!string.IsNullOrEmpty(host))
                options.LHC_API_HOST = host;
            var port = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_API_PORT");
            if (!string.IsNullOrEmpty(port))
                options.LHC_API_PORT = IntegerConverter.FromString(port);
            var apiProtocol = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_API_PROTOCOL");
            if (!string.IsNullOrEmpty(apiProtocol))
                options.LHC_API_PROTOCOL = apiProtocol;
            var clientId = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_CLIENT_ID");
            if (!string.IsNullOrEmpty(clientId))
                options.LHC_CLIENT_ID = clientId;
            var caCert = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_CA_CERT");
            if (!string.IsNullOrEmpty(caCert))
                options.LHC_CA_CERT = caCert;
            var clientCert = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_CLIENT_CERT");
            if (!string.IsNullOrEmpty(clientCert))
                options.LHC_CLIENT_CERT = clientCert;
            var clientKey = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_CLIENT_KEY");
            if (!string.IsNullOrEmpty(clientKey))
                options.LHC_CLIENT_KEY = clientKey;
            var oauthClientId = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_OAUTH_CLIENT_ID");
            if (!string.IsNullOrEmpty(oauthClientId))
                options.LHC_OAUTH_CLIENT_ID = oauthClientId;
            var oauthClientSecret = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_OAUTH_CLIENT_SECRET");
            if (!string.IsNullOrEmpty(oauthClientSecret))
                options.LHC_OAUTH_CLIENT_SECRET = oauthClientSecret;
            var oauthAccessTokenUrl = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_OAUTH_ACCESS_TOKEN_URL");
            if (!string.IsNullOrEmpty(oauthAccessTokenUrl))
                options.LHC_OAUTH_ACCESS_TOKEN_URL = oauthAccessTokenUrl;
            var numberWorkerThreads = GetValueIfKeyIsPresent(lHOptionsInFile, "LHW_NUM_WORKER_THREADS");
            if (!string.IsNullOrEmpty(numberWorkerThreads))
                options.LHW_NUM_WORKER_THREADS = IntegerConverter.FromString(numberWorkerThreads);
            var taskWorkerVersion = GetValueIfKeyIsPresent(lHOptionsInFile, "LHW_TASK_WORKER_VERSION");
            if (!string.IsNullOrEmpty(taskWorkerVersion))
                options.LHW_TASK_WORKER_VERSION = taskWorkerVersion;
            
            return options;
        }

        private static string GetValueIfKeyIsPresent(Dictionary<string, string> options, string keyName)
        {
            _tryGetValue = options.TryGetValue(keyName, out var value);
            if (_tryGetValue)
                return value!;
            
            return string.Empty;
        }
    }
}