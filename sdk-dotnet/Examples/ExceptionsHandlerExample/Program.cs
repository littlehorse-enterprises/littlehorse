using ExceptionsHandler;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace ExceptionsHandlerExample;

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

    private static LHConfig GetLHConfig(string[] args, ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);

        string filePath = Path.Combine(Directory.GetCurrentDirectory(), ".config/littlehorse.config");
        if (File.Exists(filePath))
            config = new LHConfig(filePath, loggerFactory);

        return config;
    }

    private static List<LHTaskWorker<MyWorker>> GetTaskWorkers(LHConfig config)
    {
        MyWorker executableExceptionHandling = new MyWorker();
        var workers = new List<LHTaskWorker<MyWorker>>
        {
            new(executableExceptionHandling, "fail", config),
            new(executableExceptionHandling, "fail-new-process", config),
            new(executableExceptionHandling, "technical-failure", config),
            new(executableExceptionHandling, "my-task", config)
        };
        
        return workers;
    }
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            NodeOutput node = wf.Execute("fail");
            wf.HandleError(
                node,
                handler =>
                {
                    handler.Execute("my-task");
                }
            );
            wf.Execute("my-task");
        }
        
        return new Workflow("example-exception-handler", MyEntryPoint);
    }
    
    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            var workers = GetTaskWorkers(config);

            await Task.WhenAll(workers.Select(worker => worker.RegisterTaskDef()));

            await GetWorkflow().RegisterWfSpec(config.GetGrpcClientInstance());

            await Task.Delay(300);

            await Task.WhenAll(workers.Select(worker => worker.Start()));
        }
    }
}