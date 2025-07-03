using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace ExternalEventRegisterExample;

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

    private static Dictionary<string, string> GetDictionaryFromMainArgs(string[] args)
    {
        var keyValueArgs = args.Select(item => item.Split('='))
            .ToDictionary(item => item[0], item => item[1]);

        return keyValueArgs;
    }

    private static LHConfig GetLHConfig(string[] args, ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);
        var userProfilePath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        string filePath = Path.Combine(userProfilePath, ".config/littlehorse.config");

        if (File.Exists(filePath))
            config = new LHConfig(filePath, loggerFactory);

        if (args.Length > 0)
        {
            var lhConfigs = GetDictionaryFromMainArgs(args);
            config = new LHConfig(lhConfigs, loggerFactory);
        }

        return config;
    }

    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            var key = wf.DeclareStr("key").Searchable();
            var eventResult = wf.DeclareInt("event-result");
            wf.WaitForEvent("correlated-with-deletion")
                .WithCorrelationId(key)
                .WithCorrelatedEventConfig(
                    new CorrelatedEventConfig
                    {
                        DeleteAfterFirstCorrelation = true
                    })
                .RegisteredAs(typeof(int));
            wf.Execute("print-message-externa-event", key);
            eventResult.Assign(eventResult);
        }

        return new Workflow("external-event-registration", MyEntryPoint);
    }

    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);

            var executable = new MyWorker();
            var taskWorker = new LHTaskWorker<MyWorker>(executable, "print-message-externa-event", config);
            taskWorker.RegisterTaskDef();

            var workflow = GetWorkflow();
            workflow.RegisterWfSpec(config.GetGrpcClientInstance());

            Thread.Sleep(1000);

            taskWorker.Start();
        }
    }
}