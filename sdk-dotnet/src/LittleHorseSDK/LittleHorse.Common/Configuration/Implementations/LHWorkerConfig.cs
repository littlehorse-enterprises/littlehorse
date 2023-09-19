using Grpc.Core;
using Grpc.Net.Client;
using LittleHorse.Common.Authentication;
using LittleHorse.Common.Authentication.Model;
using LittleHorse.Common.Configuration.Models;
using LittleHorse.Common.Exceptions;
using LittleHorseSDK.Common.proto;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using System.Net;
using static LittleHorseSDK.Common.proto.LHPublicApi;


namespace LittleHorse.Common.Configuration.Implementations
{
    public class LHWorkerConfig : ILHWorkerConfig
    {
        private ILogger<LHWorkerConfig>? _logger;
        private LHWorkerOptions _options;
        private Dictionary<string, GrpcChannel> _createdChannels;
        private OAuthConfig? _oAuthConfig;
        private OAuthClient? _oAuthClient;

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
                var result = !string.IsNullOrEmpty(_options.LHC_OAUTH_AUTHORIZATION_SERVER) && !string.IsNullOrEmpty(_options.LHC_OAUTH_CLIENT_ID) && !string.IsNullOrEmpty(_options.LHC_OAUTH_CLIENT_SECRET);
                if(!result)
                {
                    _logger?.LogInformation("oAuth is disable");
                }
                else
                {
                    _logger?.LogInformation("oAuth is enable");
                }

                return result;
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

                return response;
            }
            catch (RpcException ex)
            {
                _logger?.LogCritical(exception: ex, $"GetTaskDef error, taskDefName: {taskDefName}, Error Code: {ex.StatusCode}");
                throw;
            }
        }

        private GrpcChannel CreateChannel(string host, int port)
        {
            GrpcChannel channel;

            var httpHandler = new HttpClientHandler();
            var address = $"https://{host}:{port}";

            #if DEBUG
                _logger?.LogInformation("Debug Mode");
                _logger?.LogWarning("Using insecure channel!");
                address = $"http://{host}:{port}";
                httpHandler.ServerCertificateCustomValidationCallback = HttpClientHandler.DangerousAcceptAnyServerCertificateValidator;
            #endif

            channel = GrpcChannel.ForAddress(address, new GrpcChannelOptions
            {
                HttpHandler = httpHandler
            });

            _createdChannels.Add($"{host}:{port}", channel);

            return channel;
        }

        private GrpcChannel CreateChannelWithOAuthCredentials(string host, int port)
        {

            GrpcChannel channel;

            InitializeOAuth();

            if (_oAuthClient is null)
            {
                throw new LHAuthorizationServerException("OAuth is not initialized.");
            }

            var credentials = CallCredentials.FromInterceptor(async (context, metadata) =>
            {
                var tokenInfo = await _oAuthClient.GetAccessTokenAsync();
                metadata.Add("Authorization", $"Bearer {tokenInfo.Token}");
            });


            var httpHandler = new HttpClientHandler();
            var address = $"https://{host}:{port}";

            #if DEBUG
                _logger?.LogInformation("Debug Mode");
                _logger?.LogWarning("Using insecure channel!");
                address = $"http://{host}:{port}";
                httpHandler.ServerCertificateCustomValidationCallback = HttpClientHandler.DangerousAcceptAnyServerCertificateValidator;
            #endif

            channel = GrpcChannel.ForAddress(address, new GrpcChannelOptions
            {
                HttpHandler = httpHandler,
                Credentials = ChannelCredentials.Create(new SslCredentials(), credentials)
            });

            _createdChannels.Add($"{host}:{port}", channel);
            return channel;
        }

        private void InitializeOAuth()
        {
            if(_oAuthConfig is null)
            {
                _oAuthConfig = new OAuthConfig(_options.LHC_OAUTH_CLIENT_ID, _options.LHC_OAUTH_CLIENT_SECRET, _options.LHC_OAUTH_AUTHORIZATION_SERVER);

                if(_oAuthClient is null)
                {
                    _oAuthClient = new OAuthClient(_oAuthConfig, _logger);
                }
            }
        }
    }
}
