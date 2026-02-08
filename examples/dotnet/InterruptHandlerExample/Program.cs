using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace InterruptHandlerExample;

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
        var executable = new MyWorker();
        var workers = new List<LHTaskWorker<MyWorker>>
        {
            new(executable, "some-task", config),
            new(executable, "my-task", config)
        };
        
        return workers;
    }
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            // Register an interrupt handler
            wf.RegisterInterruptHandler(
                "interruption-event",
                handler =>
                {
                    handler.Execute("some-task");
                })
                .WithEventType(null);
            // Do some work that takes a while
            wf.SleepSeconds(30);
            wf.Execute("my-task");
        }
        
        return new Workflow("example-interrupt-handler", MyEntryPoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(loggerFactory);
            var client = config.GetGrpcClientInstance();
            var workers = GetTaskWorkers(config);

            await Task.WhenAll(workers.Select(worker => worker.RegisterTaskDef()));
            
            var workflow = GetWorkflow();

            await workflow.RegisterWfSpec(config.GetGrpcClientInstance());

            await Task.Delay(300);

            await Task.WhenAll(workers.Select(worker => worker.Start()));
        }
    }
}