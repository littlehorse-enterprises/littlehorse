﻿using Grpc.Core;
using Grpc.Net.Client;
using LittleHorse.Sdk.Authentication;
using LittleHorse.Common.Configuration.Models;
using LittleHorse.Common.Proto;
using LittleHorse.Sdk.Internal;
using LittleHorse.Sdk.Utils;
using Microsoft.Extensions.Logging;
using static LittleHorse.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk {

    public class LHConfig
    {
        private ILogger<LHConfig>? _logger;

        private LHOptions _options;

        private Dictionary<string, LittleHorseClient> _createdChannels;
        
        private OAuthConfig? _oAuthConfig;
        private OAuthClient? _oAuthClient;
        
        public LHConfig(ILoggerFactory? loggerFactory = null)
        {
            LHLoggerFactoryProvider.Initialize(loggerFactory);
            _logger = LHLoggerFactoryProvider.GetLogger<LHConfig>();
            _options = LHOptionsBinder.GetOptionsFromEnvironmentVariables();
            _createdChannels = new Dictionary<string, LittleHorseClient>();
        }

        public string? WorkerId
        {
            get
            {
                return _options.LHC_CLIENT_ID;
            }
        }

        public string? TaskWorkerVersion
        {
            get { return _options.LHW_TASK_WORKER_VERSION; }
        }

        public int WorkerThreads
        {
            get { return _options.LHW_NUM_WORKER_THREADS; }
        }
        public string BootstrapHost
        {
            get
            {
                return _options.LHC_API_HOST;
            }
        }
        public int BootstrapPort
        {
            get
            {
                return _options.LHC_API_PORT;
            }
        }
        public string BootstrapProtocol
        {
            get
            {
                if (_options.LHC_API_PROTOCOL != "PLAIN" && _options.LHC_API_PROTOCOL != "TLS")
                {
                    throw new ArgumentException("Invalid Protocol: " + _options.LHC_API_PROTOCOL);
                }
                return _options.LHC_API_PROTOCOL == "TLS" ? "https" : "http";
            }
        }

        public string BootstrapServer
        {
            get
            {
                return $"{BootstrapProtocol}://{BootstrapHost}:{BootstrapPort}";
            }
        }

        private bool IsOAuth
        {
            get
            {
                var result = !string.IsNullOrEmpty(_options.LHC_OAUTH_ACCESS_TOKEN_URL) 
                             && !string.IsNullOrEmpty(_options.LHC_OAUTH_CLIENT_ID) 
                             && !string.IsNullOrEmpty(_options.LHC_OAUTH_CLIENT_SECRET);
                if (!result)
                {
                    _logger?.LogInformation("OAuth is disable");
                }
                else
                {
                    _logger?.LogInformation("OAuth is enable");
                }

                return result;
            }
        }

        public LittleHorseClient GetGrcpClientInstance()
        {
            return GetGrcpClientInstance(BootstrapHost, BootstrapPort);
        }

        public LittleHorseClient GetGrcpClientInstance(string host, int port)
        {
            string channelKey = $"{BootstrapProtocol}://{host}:{port}";

            if (_createdChannels.ContainsKey(channelKey))
            {
                return _createdChannels[channelKey];
            }

            _logger?.LogInformation("Establishing connection to: " + channelKey);

            GrpcChannel channel = CreateChannel(host, port);
            
            var lhClient = new LittleHorseClient(channel);

            _createdChannels.Add(channelKey, lhClient);

            return lhClient;
        }

        private GrpcChannel CreateChannel(string host, int port)
        {
            var httpHandler = new HttpClientHandler();
            var address = $"{BootstrapProtocol}://{host}:{port}";
            
            if (_options.LHC_CA_CERT != null)
            {
                httpHandler = CertificatesHandler.GetHttpHandlerFrom(_options.LHC_CA_CERT);
            }

            if (_options.LHC_CLIENT_CERT != null && _options.LHC_CLIENT_KEY != null)
            {
                var cert = 
                    CertificatesHandler.GetX509CertificateFrom(_options.LHC_CLIENT_KEY, 
                        _options.LHC_CLIENT_CERT);
                
                httpHandler.ClientCertificates.Add(cert);
            }
            
            if (IsOAuth)
            {
                return CreateGrpcChannelWithOauthCredentials(address, httpHandler);
            }

            return GrpcChannel.ForAddress(address, new GrpcChannelOptions
            {
                HttpHandler = httpHandler
            });
        }

        private GrpcChannel CreateGrpcChannelWithOauthCredentials(string address, HttpClientHandler httpHandler)
        {
            InitializeOAuth();

            if (_oAuthClient is null)
            {
                throw new Exception("OAuth is not initialized.");
            }

            var credentials = CallCredentials.FromInterceptor(async (context, metadata) =>
            {
                var tokenInfo = await _oAuthClient.GetAccessTokenAsync();
                metadata.Add("Authorization", $"Bearer {tokenInfo.AccessToken}");
            });
            
            return GrpcChannel.ForAddress(address, new GrpcChannelOptions
            {
                HttpHandler = httpHandler,
                Credentials = ChannelCredentials.Create(new SslCredentials(), credentials)
            });
        }

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
                _logger?.LogCritical(exception: ex, $"GetTaskDef error, taskDefName: {taskDefName}, " +
                                                    $"Error Code: {ex.StatusCode}");
                throw;
            }
        }
        
        private void InitializeOAuth()
        {
            if (_oAuthConfig is null)
            {
                _oAuthConfig = new OAuthConfig(_options.LHC_OAUTH_CLIENT_ID, 
                    _options.LHC_OAUTH_CLIENT_SECRET, 
                    _options.LHC_OAUTH_ACCESS_TOKEN_URL);

                if (_oAuthClient is null)
                {
                    _oAuthClient = new OAuthClient(_oAuthConfig);
                }
            }
        }
    }
}
