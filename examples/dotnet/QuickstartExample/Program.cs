using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace QuickstartExample;

public class Program
{
    private static ServiceProvider? _serviceProvider;

    private static void SetupApplication()
    {
        _serviceProvider = new ServiceCollection()
            .AddLogging(config =>
            {
                config.AddConsole();
                config.SetMinimumLevel(LogLevel.Debug);
            })
            .BuildServiceProvider();
    }

    private static LHConfig GetLHConfig(ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);
        var userProfilePath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        string filePath = Path.Combine(userProfilePath, ".config/littlehorse.config");

        if (File.Exists(filePath))
        {
            config = new LHConfig(filePath, loggerFactory);
        }

        return config;
    }

    public static async Task Main(string[] args)
    {
        _ = args;
        SetupApplication();

        if (_serviceProvider == null)
        {
            return;
        }

        var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
        var config = GetLHConfig(loggerFactory);
        var client = config.GetGrpcClientInstance();
        var tasks = new KnowYourCustomerTasks();
        var workers = new[]
        {
            new LHTaskWorker<KnowYourCustomerTasks>(tasks, QuickstartWorkflow.VerifyIdentityTask, config),
            new LHTaskWorker<KnowYourCustomerTasks>(tasks, QuickstartWorkflow.NotifyCustomerVerifiedTask, config),
            new LHTaskWorker<KnowYourCustomerTasks>(tasks, QuickstartWorkflow.NotifyCustomerNotVerifiedTask, config),
        };

        client.PutExternalEventDef(new PutExternalEventDefRequest
        {
            Name = QuickstartWorkflow.IdentityVerifiedEvent,
            CorrelatedEventConfig = new CorrelatedEventConfig(),
        });

        await Task.WhenAll(workers.Select(worker => worker.RegisterTaskDef()));
        await QuickstartWorkflow.GetWorkflow().RegisterWfSpec(client);

        Console.WriteLine("Registered quickstart metadata and starting task workers.");
        await Task.WhenAll(workers.Select(worker => worker.Start()));
    }
}
