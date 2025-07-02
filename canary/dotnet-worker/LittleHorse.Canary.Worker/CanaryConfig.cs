using Confluent.Kafka;
using Littlehorse;
using LittleHorse.Sdk;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Canary.Worker;

public class CanaryConfig(IConfiguration configs)
{

    private const string TopicNameProperty = "topic.name";
    private const string MetronomeWorkerEnableProperty = "metronome.worker.enable";
    private const string MetronomeBeatExtraTagsProperty = "metronome.beat.extra.tags";
    private const string MetronomeBeatExtraTagsPrefixProperty = $"{MetronomeBeatExtraTagsProperty}.";

    private readonly Dictionary<string, string> _kafkaConfigs = configs.GetSection("Kafka").ToDictionary(true);
    private readonly Dictionary<string, string> _canaryConfigs = configs.GetSection("Canary").ToDictionary(true);
    private readonly Dictionary<string, string> _lhConfigs = configs.GetSection("LH").ToDictionary();

    public LHConfig GetLHConfig(ILoggerFactory? loggerFactory = null)
    {
        return new LHConfig(_lhConfigs, loggerFactory);
    }
    public ProducerConfig ProducerConfig => new(_kafkaConfigs);
    public string TopicName =>
        _canaryConfigs[TopicNameProperty] ??
        throw new ArgumentNullException($"Variable {TopicNameProperty} is required");
    public bool MetronomeWorkerEnable =>
        _canaryConfigs[MetronomeWorkerEnableProperty] != null ?
            bool.Parse(_canaryConfigs[MetronomeWorkerEnableProperty]) :
            throw new ArgumentNullException($"Variable {MetronomeWorkerEnableProperty} is required");
    public List<Tag> MetronomeExtraTags => _canaryConfigs
        .Where(config => config.Key.StartsWith(MetronomeBeatExtraTagsPrefixProperty))
        .Select(config => new Tag { Key = config.Key[MetronomeBeatExtraTagsPrefixProperty.Length..], Value = config.Value })
        .ToList();
}
