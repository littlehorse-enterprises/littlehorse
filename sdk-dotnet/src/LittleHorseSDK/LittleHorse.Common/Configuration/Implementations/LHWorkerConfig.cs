using Grpc.Core;
using Grpc.Net.Client;
using LittleHorse.Common.Configuration.Models;
using LittleHorseSDK.Common.proto;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using static LittleHorseSDK.Common.proto.LHPublicApi;


namespace LittleHorse.Common.Configuration.Implementations
{
    public class LHWorkerConfig : ILHWorkerConfig
    {
        private ILogger<LHWorkerConfig>? _logger;
        private LHWorkerOptions _options;
        private Dictionary<string, GrpcChannel> _createdChannels;

        public string APIBootstrapHost
        {
            get
            {
                return _options.LHC_API_HOST;
            }
        }
        public int APIBootstrapPort
        {
            get
            {
                return _options.LHC_API_PORT;
            }
        }
        public string ClientId
        {
            get
            {
                return _options.LHC_CLIENT_ID;
            }
        }
        public string TaskWorkerVersion
        {
            get { return _options.LHW_TASK_WORKER_VERSION; }
        }
        public string ConnectListener
        {
            get { return _options.LHW_SERVER_CONNECT_LISTENER; }
        }
        public int WorkerThreads
        {
            get { return _options.LHW_NUM_WORKER_THREADS; }
        }
        public bool IsOAuth
        {
            get
            {
                return !string.IsNullOrEmpty(_options.LHC_OAUTH_AUTHORIZATION_SERVER) || !string.IsNullOrEmpty(_options.LHC_OAUTH_CLIENT_ID) || !string.IsNullOrEmpty(_options.LHC_OAUTH_CLIENT_SECRET);
            }
        }
        public int ActiveChannelsCount
        {
            get { return _createdChannels.Count; }
        }

        public LHWorkerConfig(IConfiguration configuration, ILogger<LHWorkerConfig>? logger = null)
        {
            _logger = logger;

            _options = new LHWorkerOptions();
            configuration.Bind(_options);

            _logger?.LogInformation(APIBootstrapHost + ":" + APIBootstrapPort);
            _createdChannels = new Dictionary<string, GrpcChannel>();
        }

        /// <summary>
        /// Gets a client for the LH Public API on the configured host/port, which is
        /// generally the loadbalancer url.
        /// </summary>
        /// <returns>Client for the configured host/port.</returns>
        public LHPublicApiClient GetGrcpClientInstance()
        {
            return GetGrcpClientInstance(APIBootstrapHost, APIBootstrapPort);
        }

        /// <summary>
        /// Gets client for the LH Public API on a specified host and port. Generally used
        /// by the Task Worker, which needs to connect directly to a specific LH Server rather than the
        /// bootstrap host(loadbalancer).
        /// </summary>
        /// <param name="host">Host that the LH Server lives on.</param>
        /// <param name="port">Port that the LH Server lives on.</param>
        /// <returns>Client for the host/port combo.</returns>
        public LHPublicApiClient GetGrcpClientInstance(string host, int port)
        {
            GrpcChannel channel;

            string channelkey = $"{host}:{port}";

            if (_createdChannels.ContainsKey(channelkey))
            {
                channel = _createdChannels[channelkey];
            }
            else if (IsOAuth)
            {
                channel = CreateChannelWithOAuthCredentials(host, port);
            }
            else
            {
                channel = CreateChannel(host, port);
            }

            return new LHPublicApiClient(channel);
        }

        /// <summary>
        /// Gets the `TaskDefPb` for a given taskDefName.
        /// </summary>
        /// <param name="taskDefName">The TaskDef's name.</param>
        /// <returns>The specified TaskDefPb.</returns>
        /// <exception cref="Exception">If taskdef loading fails.</exception>
        public TaskDef GetTaskDef(string taskDefName)
        {
            try
            {
                var client = GetGrcpClientInstance();
                var taskDefId = new TaskDefId()
                {
                    Name = taskDefName
                };
                var response = client.GetTaskDef(taskDefId);

                if (response.Code != LHResponseCode.Ok)
                {
                    if (response.HasMessage)
                    {
                        throw new Exception($"Failed loading taskDef: {response.Message}");
                    }

                    throw new Exception("Failed loading taskDef: No response message.");
                }

                return response.Result;
            }
            catch (Exception ex)
            {
                _logger?.LogCritical(exception: ex, "GetTaskDef error, taskDefName: {}", taskDefName);
                throw;
            }
        }

        private GrpcChannel CreateChannel(string host, int port)
        {
            GrpcChannel channel;

            if (string.IsNullOrEmpty(_options.LHC_CA_CERT))
            {
                _logger?.LogWarning("Using insecure channel!");
                AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", true);
                channel = GrpcChannel.ForAddress($"https://{host}:{port}");
            }
            else
            {
                _logger?.LogInformation("Using secure connection!");

                var credentials = new SslCredentials(File.ReadAllText(_options.LHC_CA_CERT));

                if (!string.IsNullOrEmpty(_options.LHC_CLIENT_CERT) && !string.IsNullOrEmpty(_options.LHC_CLIENT_KEY))
                {
                    _logger?.LogInformation("Using mtls!");
                    var keyCertPair = new KeyCertificatePair(File.ReadAllText(_options.LHC_CLIENT_CERT), File.ReadAllText(_options.LHC_CLIENT_KEY));

                    if(credentials?.RootCertificates is not null)
                    {
                        credentials = new SslCredentials(credentials.RootCertificates, keyCertPair);
                    }
                }

                AppContext.SetSwitch("System.Net.Http.SocketsHttpHandler.Http2UnencryptedSupport", false);
                channel = GrpcChannel.ForAddress($"https://{host}:{port}", new GrpcChannelOptions
                {
                    Credentials = credentials
                });
            }

            _createdChannels.Add($"{host}:{port}", channel);
            return channel;
        }

        private GrpcChannel CreateChannelWithOAuthCredentials(string host, int port)
        {
            //Implement OAuth, for now it will call CreateGrpcChannel
            return CreateChannel(host, port);
        }
    }
}
