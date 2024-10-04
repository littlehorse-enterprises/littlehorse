using System.Runtime.CompilerServices;
using LittleHorse.Sdk.Utils;

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
            Dictionary<string, string> lHOptionsInFile = GetDictFromPath(filePath);

            var host = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_API_HOST");
            if (!string.IsNullOrEmpty(host))
                LHC_API_HOST = host;
            var port = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_API_PORT");
            if (!string.IsNullOrEmpty(port))
                LHC_API_PORT = IntegerConverter.FromString(port);
            var apiProtocol = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_API_PROTOCOL");
            if (!string.IsNullOrEmpty(apiProtocol))
                LHC_API_PROTOCOL = apiProtocol;
            var clientId = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_CLIENT_ID");
            if (!string.IsNullOrEmpty(clientId))
                LHC_CLIENT_ID = clientId;
            var caCert = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_CA_CERT");
            if (!string.IsNullOrEmpty(caCert))
                LHC_CA_CERT = caCert;
            var clientCert = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_CLIENT_CERT");
            if (!string.IsNullOrEmpty(clientCert))
                LHC_CLIENT_CERT = clientCert;
            var clientKey = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_CLIENT_KEY");
            if (!string.IsNullOrEmpty(clientKey))
                LHC_CLIENT_KEY = clientKey;
            var oauthClientId = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_OAUTH_CLIENT_ID");
            if (!string.IsNullOrEmpty(oauthClientId))
                LHC_OAUTH_CLIENT_ID = oauthClientId;
            var oauthClientSecret = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_OAUTH_CLIENT_SECRET");
            if (!string.IsNullOrEmpty(oauthClientSecret))
                LHC_OAUTH_CLIENT_SECRET = oauthClientSecret;
            var oauthAccessTokenUrl = GetValueIfKeyIsPresent(lHOptionsInFile, "LHC_OAUTH_ACCESS_TOKEN_URL");
            if (!string.IsNullOrEmpty(oauthAccessTokenUrl))
                LHC_OAUTH_ACCESS_TOKEN_URL = oauthAccessTokenUrl;
            var numberWorkerThreads = GetValueIfKeyIsPresent(lHOptionsInFile, "LHW_NUM_WORKER_THREADS");
            if (!string.IsNullOrEmpty(numberWorkerThreads))
                LHW_NUM_WORKER_THREADS = IntegerConverter.FromString(numberWorkerThreads);
            var taskWorkerVersion = GetValueIfKeyIsPresent(lHOptionsInFile, "LHW_TASK_WORKER_VERSION");
            if (!string.IsNullOrEmpty(taskWorkerVersion))
                LHW_TASK_WORKER_VERSION = taskWorkerVersion;
        }

        private static string GetValueIfKeyIsPresent(Dictionary<string, string> pairInFile, string keyName)
        {
            var tryGetValue = pairInFile.TryGetValue(keyName, out var value);
            if (tryGetValue)
                return value!;

            return string.Empty;
        }
        
        private static Dictionary<string, string> GetDictFromPath(string filePath)
        {
            try
            {
                Dictionary<string, string> properties = new Dictionary<string, string>();
                foreach (var tuples in File.ReadLines(filePath)
                             .Select(line => line.Trim().Split('='))
                             .Select(arr => (Key: arr[0].Trim(), Value: arr[1].Trim()))
                             .Where(tuple => !tuple.Key.TrimStart().StartsWith("#") || tuple.Value.Trim() != "")
                             .GroupBy(x => x.Key))
                    properties.Add(tuples.Key, tuples.First().Value);

                return properties;
            }
            catch (Exception ex)
            {
                if (ex is FileNotFoundException)
                    throw new FileNotFoundException($"File {filePath} does not exist.", 
                        ex.Message);
                throw;
            }
        }
    }
}
