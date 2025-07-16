using ExternalEventExample;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

public abstract class Program
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

    private static LHConfig GetConfig(string[] args, ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);

        string filePath = Path.Combine(Directory.GetCurrentDirectory(), ".config/littlehorse.config");
        if (File.Exists(filePath))
            config = new LHConfig(filePath, loggerFactory);

        return config;
    }

    private static List<LHTaskWorker<WaitForExternalEventWorker>> GetTaskWorkers(LHConfig config)
    {
        var executable = new WaitForExternalEventWorker();
        var workers = new List<LHTaskWorker<WaitForExternalEventWorker>>
        {
            new(executable, "greet", config),
            new(executable, "summary", config)
        };

        return workers;
    }

    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            var name = wf.WaitForEvent("what-is-your-name");
            var id = wf.Execute("greet", name);
            var age = wf.WaitForEvent("how-old-are-you")
                .WithCorrelationId(id, true)
                .WithCorrelatedEventConfig(new CorrelatedEventConfig
                {
                    DeleteAfterFirstCorrelation = false
                })
                .RegisteredAs(typeof(int));
            wf.WaitForEvent("allow-show-summary").RegisteredAs(null);
            wf.Execute("summary", name, age);
        }

        return new Workflow("example-external-event", MyEntryPoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetConfig(args, loggerFactory);
            var client = config.GetGrpcClientInstance();
            var workers = GetTaskWorkers(config);

            await Task.WhenAll(workers.Select(worker => worker.RegisterTaskDef()));

            var workflow = GetWorkflow();
         
            client.PutExternalEventDef(new PutExternalEventDefRequest { Name = "what-is-your-name" });

            await workflow.RegisterWfSpec(config.GetGrpcClientInstance());

            await Task.Delay(300);

            await Task.WhenAll(workers.Select(worker => worker.Start()));
        }
    }
}