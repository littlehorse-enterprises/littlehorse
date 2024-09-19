using System.Runtime.InteropServices;
using System.Security.Cryptography.X509Certificates;
using Grpc.Core;
using Grpc.Net.Client;
using LittleHorse.Common.Configuration.Models;
using LittleHorse.Common.Proto;
using LittleHorse.Sdk.Internal;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using static LittleHorse.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk {

    public class LHConfig
    {

        private ILogger<LHConfig>? _logger;

        private LHWorkerOptions _options;

        private Dictionary<string, GrpcChannel> _createdChannels;
        
        public LHConfig(IConfiguration configuration, ILoggerFactory? loggerFactory = null)
        {
            LHLoggerFactoryProvider.Initialize(loggerFactory);
            _logger = LHLoggerFactoryProvider.GetLogger<LHConfig>();
            _options = new LHWorkerOptions();
            configuration.Bind(_options);
            _createdChannels = new Dictionary<string, GrpcChannel>();
        }

        public string WorkerId
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

        public LittleHorseClient GetGrcpClientInstance()
        {
            return GetGrcpClientInstance(BootstrapHost, BootstrapPort);
        }

        public LittleHorseClient GetGrcpClientInstance(string host, int port)
        {
            string channelKey = $"{BootstrapProtocol}://{host}:{port}";

            if (_createdChannels.ContainsKey(channelKey))
            {
                return new LittleHorseClient(_createdChannels[channelKey]);
            }

            _logger?.LogInformation("Establishing connection to: " + channelKey);

            GrpcChannel channel = CreateChannel(host, port);

            _createdChannels.Add(channelKey, channel);

            return new LittleHorseClient(channel);
        }

        private GrpcChannel CreateChannel(string host, int port)
        {
            var httpHandler = new HttpClientHandler();
            var address = $"{BootstrapProtocol}://{host}:{port}";

            if (_options.LHC_CLIENT_CERT != null && _options.LHC_CLIENT_KEY != null)
            {
                string certificatePem = File.ReadAllText(_options.LHC_CLIENT_CERT);
                string privateKeyPem = File.ReadAllText(_options.LHC_CLIENT_KEY);
                X509Certificate2 cert = X509Certificate2.CreateFromPem(certificatePem, privateKeyPem);
                if (RuntimeInformation.IsOSPlatform(OSPlatform.Windows))
                {
                    var originalCert = cert;
                    cert = new X509Certificate2(cert.Export(X509ContentType.Pkcs12));
                    originalCert.Dispose();
                }
                httpHandler.ClientCertificates.Add(cert);
            }

            return GrpcChannel.ForAddress(address, new GrpcChannelOptions
            {
                HttpHandler = httpHandler
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
                _logger?.LogCritical(exception: ex, $"GetTaskDef error, taskDefName: {taskDefName}, Error Code: {ex.StatusCode}");
                throw;
            }
        }
    }
}
