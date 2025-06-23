using Confluent.Kafka;
using Littlehorse;
using LittleHorse.Sdk;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Canary.Worker;

public class CanaryConfig
{
    // TODO: check naming convention for const in C#
    private const string _TopicName = "topic.name";
    private const string _MetronomeWorkerEnable = "metronome.worker.enable";
    private const string _MetronomeBeatExtraTags = "metronome.beat.extra.tags";
    private const string _MetronomeBeatExtraTagsPrefix = $"{_MetronomeBeatExtraTags}.";

    private readonly Dictionary<string, string> _kafkaConfigs;
    private readonly IConfiguration _canaryConfigs;
    private readonly Dictionary<string, string> _lhConfigs;
    public CanaryConfig(IConfiguration configs)
    {
        _kafkaConfigs = configs.GetSection("Kafka").GetChildren().Where(config => config.Value != null).ToDictionary(field => field.Key, x => x.Value!);
        _canaryConfigs = configs.GetSection("Canary");
        _lhConfigs = configs.GetSection("LH").GetChildren().Where(config => config.Value != null).ToDictionary(field => field.Key, x => x.Value!);
    }

    public LHConfig GetLHConfig(ILoggerFactory? loggerFactory = null)
    {
        return new LHConfig(_lhConfigs, loggerFactory);
    }
    public ProducerConfig ProducerConfig => new(_kafkaConfigs);
    public string TopicName => _canaryConfigs[_TopicName] ?? throw new ArgumentNullException($"Variable {_TopicName} is required");
    public bool MetronomeWorkerEnable =>  _canaryConfigs[_MetronomeWorkerEnable] != null ? bool.Parse(_canaryConfigs[_MetronomeWorkerEnable]!) : throw new ArgumentNullException($"Variable {_MetronomeWorkerEnable} is required");
    public List<Tag> MetronomeExtraTags => _canaryConfigs
        .GetChildren()
        .ToDictionary(config => config.Key, x => x.Value)
        .Where(config => config.Key.StartsWith(_MetronomeBeatExtraTagsPrefix))
        .Select(config => new Tag {Key = config.Key[_MetronomeBeatExtraTagsPrefix.Length..], Value = config.Value})
        .ToList();
}