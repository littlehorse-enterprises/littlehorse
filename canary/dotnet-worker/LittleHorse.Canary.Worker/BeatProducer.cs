using Confluent.Kafka;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
using Littlehorse;
using Microsoft.Extensions.Logging;

namespace LittleHorse.Canary.Worker;

public class BeatProducer(
    ILogger<BeatProducer> logger,
    IProducer<byte[], byte[]> producer,
    List<Tag> extraTags,
    ServerInfo serverInfo,
    string topic)
{
    public async Task Send(TimeSpan latency, string id)
    {
        try
        {
            var key = GetBeatKey(id);
            var value = GetBeatValue(latency);
            var message = new Message<byte[], byte[]> { Key = key.ToByteArray(), Value = value.ToByteArray() };
            await producer.ProduceAsync(topic, message);
            logger.LogDebug("Produced massage {}", key.Type);
        }
        catch (Exception exception)
        {
            logger.LogError(exception, "Error producing message {}", BeatType.TaskRunExecution);
            throw;
        }
    }

    private static BeatValue GetBeatValue(TimeSpan latency)
    {
        var value = new BeatValue
        {
            Latency = latency.Milliseconds,
            Time = DateTime.UtcNow.ToTimestamp()
        };
        return value;
    }

    private BeatKey GetBeatKey(string id)
    {
        var key = new BeatKey
        {
            ServerHost = serverInfo.Host,
            ServerPort = serverInfo.Port,
            Type = BeatType.TaskRunExecution,
            Id = id
        };
        key.Tags.Add(extraTags);
        return key;
    }
}
