using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace ExternalEventRegisterExample;

public class Program
{
    private const string WorkflowName = "correlated-external-event-registration";
    private const string EventName = "sign-doc";
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

    private static LHConfig GetLHConfig(string[] args, ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);

        string filePath = Path.Combine(Directory.GetCurrentDirectory(), ".config/littlehorse.config");
        if (File.Exists(filePath))
            config = new LHConfig(filePath, loggerFactory);

        return config;
    }

    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            var input = wf.DeclareStr("doc-input").Searchable();
            var id = wf.Execute(MyWorker.CreateDoc, input);
            wf.WaitForEvent(EventName)
                .WithCorrelationId(id)
                .WithCorrelatedEventConfig(
                    new CorrelatedEventConfig
                    {
                        DeleteAfterFirstCorrelation = true
                    })
                .RegisteredAs(typeof(string));
        }

        return new Workflow(WorkflowName, MyEntryPoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);

            var executable = new MyWorker();
            var taskWorker = new LHTaskWorker<MyWorker>(executable, MyWorker.CreateDoc, config);
            await taskWorker.RegisterTaskDef();

            var workflow = GetWorkflow();
            await workflow.RegisterWfSpec(config.GetGrpcClientInstance());
            await taskWorker.Start();
        }
    }
}