using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace EventRegistrationExample;

public class Program
{
    private const string WorkflowName = "workflow-and-external-event-registration";
    private const string EventName = "doc-name";
    private const string ThrowEventName = "doc-created";

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

    private static Workflow GetWorkflow()
    {
        return new Workflow(WorkflowName, MyEntryPoint);

        void MyEntryPoint(WorkflowThread wf)
        {
            var docName = wf.WaitForEvent(EventName).RegisteredAs(typeof(string));
            var doc = wf.Execute(MyWorker.CreateDoc, docName);
            wf.ThrowEvent(ThrowEventName, doc)
                .RegisteredAs(typeof(string));
        }
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetConfig(args, loggerFactory);

            var executable = new MyWorker();
            var taskWorker = new LHTaskWorker<MyWorker>(executable, MyWorker.CreateDoc, config);
            await taskWorker.RegisterTaskDef();

            var workflow = GetWorkflow();
            await workflow.RegisterWfSpec(config.GetGrpcClientInstance());
            await taskWorker.Start();
        }
    }
}