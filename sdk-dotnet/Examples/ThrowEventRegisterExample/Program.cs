using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace ThrowEventRegisterExample;

public class Program
{
    private const string WorkflowName = "workflow-event-registration";
    private const string EventName = "event-name";
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
        
        string filePath = Path.Combine(Directory.GetCurrentDirectory(), ".config/littlehorse.config");
        if (File.Exists(filePath))
            config = new LHConfig(filePath, loggerFactory);

        return config;
    }

    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            var name = wf.DeclareStr("input-name").Searchable();
            wf.Execute(MyWorker.PrintMessageTaskDefName, name);
            wf.ThrowEvent(EventName, name)
                .RegisteredAs(typeof(string));
        }
        
        return new Workflow(WorkflowName, MyEntryPoint);
    }

    static async Task  Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);

            var executable = new MyWorker();
            var taskWorker = new LHTaskWorker<MyWorker>(executable, MyWorker.PrintMessageTaskDefName, config);
            await taskWorker.RegisterTaskDef();
            
            var workflow = GetWorkflow();
            await workflow.RegisterWfSpec(config.GetGrpcClientInstance());
            await taskWorker.Start();
        }
    }
}