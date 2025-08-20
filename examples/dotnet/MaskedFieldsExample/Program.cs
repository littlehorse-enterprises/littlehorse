using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;

namespace MaskedFieldsExample;

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

    private static LHConfig GetLHConfig(ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);
        var userProfilePath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        string filePath = Path.Combine(userProfilePath, ".config/littlehorse.config");
        
        if (File.Exists(filePath))
            config = new LHConfig(filePath, loggerFactory);

        return config;
    }
    
    private static List<LHTaskWorker<MyWorker>> GetTaskWorkers(LHConfig config)
    {
        MyWorker executableExceptionHandling = new MyWorker();
        var workers = new List<LHTaskWorker<MyWorker>>
        {
            new(executableExceptionHandling, "create-greet", config),
            new(executableExceptionHandling, "update-greet", config),
            new(executableExceptionHandling, "delete-greet", config)
        };
        
        return workers;
    }

    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            WfRunVariable maskedName = wf.DeclareStr("masked-name").Masked();
            wf.Execute("create-greet", maskedName);
            wf.Execute("update-greet", maskedName);
            WfRunVariable name = wf.DeclareStr("input-name");
            wf.Execute("delete-greet", name);
        }
        return new Workflow("example-masked-fields", MyEntryPoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(loggerFactory);
            var workers = GetTaskWorkers(config);

            await Task.WhenAll(workers.Select(worker => worker.RegisterTaskDef()));
            
            var workflow = GetWorkflow();
            await workflow.RegisterWfSpec(config.GetGrpcClientInstance());

            await Task.Delay(300);

            await Task.WhenAll(workers.Select(worker => worker.Start()));
        }
    }
}