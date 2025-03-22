using System.Net;
using Grpc.Core;
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

    public class LHConfig
    {
        private ILogger<LHConfig>? _logger;

        private LHInputVariables _inputVariables;

        private Dictionary<string, LittleHorseClient> _createdChannels;
        
        private OAuthConfig? _oAuthConfig;
        private OAuthClient? _oAuthClient;
        private const string DefaultProtocol = "PLAINTEXT";
        
        public LHConfig(ILoggerFactory? loggerFactory = null)
        {
            LHLoggerFactoryProvider.Initialize(loggerFactory);
            _logger = LHLoggerFactoryProvider.GetLogger<LHConfig>();
            _inputVariables = new LHInputVariables();
            _createdChannels = new Dictionary<string, LittleHorseClient>();
        }
        
        public LHConfig(string configOptionsFilePath, ILoggerFactory? loggerFactory = null)
        {
            LHLoggerFactoryProvider.Initialize(loggerFactory);
            _logger = LHLoggerFactoryProvider.GetLogger<LHConfig>();
            _inputVariables = new LHInputVariables(configOptionsFilePath);
            _createdChannels = new Dictionary<string, LittleHorseClient>();
        }
        
        public LHConfig(Dictionary<string, string> configArgs, ILoggerFactory? loggerFactory = null)
        {
            LHLoggerFactoryProvider.Initialize(loggerFactory);
            _logger = LHLoggerFactoryProvider.GetLogger<LHConfig>();
            _inputVariables = new LHInputVariables(configArgs);
            _createdChannels = new Dictionary<string, LittleHorseClient>();
        }

        public string? WorkerId
        {
            get
            {
                return _inputVariables.LHC_CLIENT_ID;
            }
        }

        public string? TaskWorkerVersion
        {
            get { return _inputVariables.LHW_TASK_WORKER_VERSION; }
        }

        public int WorkerThreads
        {
            get { return _inputVariables.LHW_NUM_WORKER_THREADS; }
        }
        public string BootstrapHost
        {
            get
            {
                return _inputVariables.LHC_API_HOST;
            }
        }
        public int BootstrapPort
        {
            get
            {
                return _inputVariables.LHC_API_PORT;
            }
        }

        public string ApiProtocol
        {
            get
            {
                return _inputVariables.LHC_API_PROTOCOL;
            }
        }
        
        public string? TenantId
        {
            get
            {
                return _inputVariables.LHC_TENANT_ID;
            }
        }
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

        public LittleHorseClient GetGrpcClientInstance()
        {
            return GetGrpcClientInstance(BootstrapHost, BootstrapPort);
        }

        [MethodImpl(MethodImplOptions.Synchronized)]
        public LittleHorseClient GetGrpcClientInstance(string host, int port)
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

        public TaskDef GetTaskDef(string taskDefName)
        {
            try
            {
                var client = GetGrpcClientInstance();
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
