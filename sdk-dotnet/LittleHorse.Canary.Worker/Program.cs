using Confluent.Kafka;

namespace LittleHorse.Canary.Worker;

using Microsoft.Extensions.Logging;
using LoggingFactoryBase = Microsoft.Extensions.Logging.LoggerFactory;

public class Program
{
    private static ILoggerFactory _loggerFactory = null!;
    private static ILogger<Program> _logger = null!;

    // TODO: shutdown hook to close worker
    public static async Task Main(string[] args)
    {
        var config = ConfigLoader.Load(args.Length > 0 ? args[0] : null);
        _loggerFactory = LoggingFactoryBase.Create(builder => builder.AddConsole().AddConfiguration(config.GetSection("Logging")));
        _logger = _loggerFactory.CreateLogger<Program>();

        try
        {
            _logger.LogInformation("Starting Metronome Worker");
            var canaryConfig = new CanaryConfig(config);
            await Initialize(canaryConfig);
        }
        catch (Exception exception)
        {
            _logger.LogError(exception, "Couldn't initialize canary worker: {}", exception.Message);
            Environment.Exit(-1);
        }
    }

    private static async Task Initialize(CanaryConfig config)
    {
        var lhConfig = config.GetLHConfig(_loggerFactory);
        var serverInfo = new ServerInfo(lhConfig.BootstrapHost, lhConfig.BootstrapPort);
        var producer = new ProducerBuilder<byte[], byte[]>(config.ProducerConfig).Build();
        var beatProducer = new BeatProducer(_loggerFactory.CreateLogger<BeatProducer>(), producer, config.MetronomeExtraTags, serverInfo, config.TopicName);

        if (config.MetronomeWorkerEnable)
        {
            var metronomeWorker = new MetronomeWorker(_loggerFactory.CreateLogger<MetronomeWorker>(), beatProducer, lhConfig);
            await metronomeWorker.Start();
        }
    }
}