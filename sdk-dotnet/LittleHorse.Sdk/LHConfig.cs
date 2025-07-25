﻿using Grpc.Core;
using Grpc.Net.Client;
using LittleHorse.Sdk.Authentication;
using LittleHorse.Sdk.Utils;
using Microsoft.Extensions.Logging;
using System.Security.Cryptography.X509Certificates;
using System.Net.Security;
using System.Runtime.CompilerServices;
using LittleHorse.Sdk.Common.Proto;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk {
    
    /// <summary>
    /// This class is used to configure a LHClient.
    /// </summary>
    public class LHConfig
    {
        private ILogger<LHConfig>? _logger;

        private LHInputVariables _inputVariables;

        private Dictionary<string, GrpcChannel?> _createdChannels;
        
        private OAuthConfig? _oAuthConfig;
        private OAuthClient? _oAuthClient;
        private const string DefaultProtocol = "PLAINTEXT";
        
        /// <summary>
        /// Creates a LHConfig that loads values from environmental variables.
        /// This is the default constructor which could be initialized such as <c>new LHConfig();</c>
        /// 
        /// </summary>
        /// <param name="loggerFactory">Optional parameter that allow to propagate logs configs from parent apps.
        /// </param>
        public LHConfig(ILoggerFactory? loggerFactory = null)
        {
            LHLoggerFactoryProvider.Initialize(loggerFactory);
            _logger = LHLoggerFactoryProvider.GetLogger<LHConfig>();
            _inputVariables = new LHInputVariables();
            _createdChannels = new Dictionary<string, GrpcChannel?>();
        }
        
        /// <summary>
        /// Creates a LHConfig that loads values from a file which contains LH configs.
        /// <example>new LHConfig("file-path");</example>
        /// </summary>
        /// <param name="configOptionsFilePath">It is the file location where the LH config options are placed.</param>
        /// <param name="loggerFactory">Optional parameter that allow to propagate logs configs from parent apps.
        /// </param>
        public LHConfig(string configOptionsFilePath, ILoggerFactory? loggerFactory = null)
        {
            LHLoggerFactoryProvider.Initialize(loggerFactory);
            _logger = LHLoggerFactoryProvider.GetLogger<LHConfig>();
            _inputVariables = new LHInputVariables(configOptionsFilePath);
            _createdChannels = new Dictionary<string, GrpcChannel?>();
        }
        
        /// <summary>
        /// Creates a LHConfig that loads values from arguments being added from an app initialization.
        /// </summary>
        /// <param name="configArgs">A dictionary of arguments with LH config options.</param>
        /// <param name="loggerFactory">Optional parameter that allow to propagate logs configs from parent apps.
        /// </param>
        public LHConfig(Dictionary<string, string> configArgs, ILoggerFactory? loggerFactory = null)
        {
            LHLoggerFactoryProvider.Initialize(loggerFactory);
            _logger = LHLoggerFactoryProvider.GetLogger<LHConfig>();
            _inputVariables = new LHInputVariables(configArgs);
            _createdChannels = new Dictionary<string, GrpcChannel?>();
        }

        /// <summary>
        /// Retrieves the task worker ID from the configuration properties,
        /// </summary>
        public string WorkerId => _inputVariables.LHC_CLIENT_ID;

        /// <summary>
        /// Retrieves the task worker version from the configuration properties.
        /// </summary>
        public string TaskWorkerVersion => _inputVariables.LHW_TASK_WORKER_VERSION;

        /// <summary>
        /// Retrieves the number of worker threads from the configuration properties.
        /// </summary>
        public int WorkerThreads => _inputVariables.LHW_NUM_WORKER_THREADS;

        /// <summary>
        /// Retrieves the bootstrap host from the configuration properties.
        /// </summary>
        public string BootstrapHost => _inputVariables.LHC_API_HOST;

        /// <summary>
        /// Retrieves the bootstrap server port from the configuration properties.
        /// </summary>
        public int BootstrapPort => _inputVariables.LHC_API_PORT;

        /// <summary>
        /// Retrieves the API protocol from the configuration properties.
        /// </summary>
        public string ApiProtocol => _inputVariables.LHC_API_PROTOCOL;

        /// <summary>
        /// Retrieves the TenantId from the configuration properties.
        /// </summary>
        public string? TenantId => _inputVariables.LHC_TENANT_ID;

        /// <summary>
        /// Retrieves the Bootstrap protocol from the configuration properties.
        /// </summary>
        /// <exception cref="ArgumentException">
        /// Throws an exception when <c>LHC_API_PROTOCOL</c> is different from <c>PLAINTEXT</c> or <c>TLS</c>
        /// </exception>
        public string BootstrapProtocol
        {
            get
            {
                if (_inputVariables.LHC_API_PROTOCOL != "PLAINTEXT" && _inputVariables.LHC_API_PROTOCOL != "TLS")
                {
                    throw new ArgumentException("Invalid Protocol: " + _inputVariables.LHC_API_PROTOCOL);
                }
                return _inputVariables.LHC_API_PROTOCOL == "TLS" ? "https" : "http";
            }
        }

        /// <summary>
        /// Retrieves the Bootstrap server url.
        /// <example>https://localhost:2023</example>
        /// </summary>
        public string BootstrapServer => $"{BootstrapProtocol}://{BootstrapHost}:{BootstrapPort}";

        private bool IsOAuth
        {
            get
            {
                var result = !string.IsNullOrEmpty(_inputVariables.LHC_OAUTH_ACCESS_TOKEN_URL) 
                             && !string.IsNullOrEmpty(_inputVariables.LHC_OAUTH_CLIENT_ID) 
                             && !string.IsNullOrEmpty(_inputVariables.LHC_OAUTH_CLIENT_SECRET);
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

        /// <summary>
        /// Retrieves the LittleHorse Grpc client connected to a boostrap server.
        /// </summary>
        /// <returns>A LittleHorseClient</returns>
        public LittleHorseClient GetGrpcClientInstance()
        {
            return GetGrpcClientInstance(BootstrapHost, BootstrapPort);
        }

        /// <summary>
        /// Retrieves the LittleHorse Grpc client connected to a specific channel.
        /// </summary>
        /// <param name="host">Host value</param>
        /// <param name="port">Port value</param>
        /// <returns>A LittleHorseClient</returns>
        [MethodImpl(MethodImplOptions.Synchronized)]
        public LittleHorseClient GetGrpcClientInstance(string host, int port)
        {
            string channelKey = $"{BootstrapProtocol}://{host}:{port}";

            if (!_createdChannels.TryGetValue(channelKey, out var channel))
            {
                _logger?.LogInformation("Establishing connection to: " + channelKey);
                channel = CreateChannel(host, port);
                _createdChannels.Add(channelKey, channel);
            }

            return new LittleHorseClient(channel);
        }

        private GrpcChannel CreateChannel(string host, int port)
        {
            var httpHandler = new HttpClientHandler();
            var address = $"{BootstrapProtocol}://{host}:{port}";
            httpHandler.ServerCertificateCustomValidationCallback = ServerCertificateCustomValidation;
            
            var tenantCredentials = CallCredentials.FromInterceptor((context, metadata) =>
            {
                if (TenantId != null && TenantId.Length > 0)
                {
                    metadata.Add("tenantid", TenantId);
                }
                return Task.CompletedTask;
            });
            
            if (_inputVariables.LHC_CLIENT_CERT != null && _inputVariables.LHC_CLIENT_KEY != null)
            {
                var cert = 
                    CertificatesHandler.GetX509CertificateFrom(_inputVariables.LHC_CLIENT_KEY, 
                        _inputVariables.LHC_CLIENT_CERT);
                
                httpHandler.ClientCertificates.Add(cert);
            }

            if (IsOAuth)
            {
                return CreateGrpcChannelWithOauthCredentials(address, httpHandler, tenantCredentials);
            }

            var channelCredentials = ApiProtocol.Equals(DefaultProtocol) ? ChannelCredentials.Insecure : new SslCredentials();
            return GrpcChannel.ForAddress(address, new GrpcChannelOptions
            {
                HttpHandler = httpHandler,
                Credentials = ChannelCredentials.Create(channelCredentials, tenantCredentials),
                UnsafeUseInsecureChannelCallCredentials = channelCredentials == ChannelCredentials.Insecure
            });
        }

        private bool ServerCertificateCustomValidation(HttpRequestMessage requestMessage, X509Certificate2? certificate, X509Chain? certChain, SslPolicyErrors sslErrors)
        {
            var pathCaCert = _inputVariables.LHC_CA_CERT;
            if (pathCaCert != null)
            {
                var caCert = new X509Certificate2(File.ReadAllBytes(pathCaCert));

                certChain!.ChainPolicy.TrustMode = X509ChainTrustMode.CustomRootTrust;
                certChain.ChainPolicy.CustomTrustStore.Add(caCert);
            }

            var certChainBuilder = certificate != null && certChain != null && certChain.Build(certificate);
            return certChainBuilder;
        }

        private GrpcChannel CreateGrpcChannelWithOauthCredentials(string address, HttpClientHandler httpHandler, CallCredentials tenantCredentials)
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
                Credentials = ChannelCredentials.Create(new SslCredentials(), CallCredentials.Compose(credentials, tenantCredentials))
            });
        }

        /// <summary>
        /// Retrieves a specific TaskDef searched by its Name.
        /// </summary>
        /// <param name="taskDefName">The name of the TaskDef</param>
        /// <returns>A TaskDef</returns>
        public TaskDef GetTaskDef(string taskDefName)
        {
            try
            {
                var client = GetGrpcClientInstance();
                var taskDefId = new TaskDefId
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
                _oAuthConfig = new OAuthConfig(_inputVariables.LHC_OAUTH_CLIENT_ID, 
                    _inputVariables.LHC_OAUTH_CLIENT_SECRET, 
                    _inputVariables.LHC_OAUTH_ACCESS_TOKEN_URL);

                if (_oAuthClient is null)
                {
                    _oAuthClient = new OAuthClient(_oAuthConfig);
                }
            }
        }
    }
}
