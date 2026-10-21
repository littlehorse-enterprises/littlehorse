using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Grpc.Core;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Worker.Internal;
using Moq;
using Xunit;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;

namespace LittleHorse.Sdk.Tests.Worker;

public class LHServerConnectionManagerTest
{
    public LHServerConnectionManagerTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
    }

    [Theory]
    [InlineData(false)]
    [InlineData(true)]
    public async Task InitialFailuresRetryQuicklyAndLaterFailuresKeepNormalInterval(bool firstRegistrationSucceeds)
    {
        var client = new Mock<LittleHorseClient>();
        client.Setup(c => c.GetTaskDefAsync(It.IsAny<TaskDefId>(), It.IsAny<Metadata>(),
                It.IsAny<DateTime?>(), It.IsAny<CancellationToken>()))
            .Returns(() => UnaryCall(Task.FromResult(new TaskDef
            {
                Id = new TaskDefId { Name = "retry-task" }
            })));
        var failures = firstRegistrationSucceeds
            ? new[] { false, true }
            : new[] { true, true, false, true };
        var attempts = 0;
        client.Setup(c => c.RegisterTaskWorkerAsync(It.IsAny<RegisterTaskWorkerRequest>(), It.IsAny<Metadata>(),
                It.IsAny<DateTime?>(), It.IsAny<CancellationToken>()))
            .Returns(() => failures[attempts++]
                ? UnaryCall(Task.FromException<RegisterTaskWorkerResponse>(
                    new RpcException(new Status(StatusCode.Unavailable, "unavailable"))))
                : UnaryCall(Task.FromResult(new RegisterTaskWorkerResponse())));

        var task = new LHTask<Worker>(new Worker(), "retry-task", client.Object);
        await task.PrepareLHTaskMethod();
        using var cancellation = new CancellationTokenSource();
        var delays = new List<int>();
        var manager = new LHServerConnectionManager<Worker>(
            new LHConfig(new Dictionary<string, string>()), task, client.Object, cancellation.Token,
            milliseconds =>
            {
                delays.Add(milliseconds);
                if (delays.Count == failures.Length) cancellation.Cancel();
                return Task.CompletedTask;
            });

        await manager.Start();

        Assert.Equal(firstRegistrationSucceeds ? new[] { 60000, 60000 } : new[] { 1000, 1000, 60000, 60000 }, delays);
        Assert.Equal(failures.Length, attempts);
    }

    private static AsyncUnaryCall<T> UnaryCall<T>(Task<T> response) =>
        new(response, Task.FromResult(new Metadata()), () => Status.DefaultSuccess, () => new Metadata(), () => { });

    public class Worker
    {
        [LHTaskMethod("retry-task")]
        public void Execute() { }
    }
}
