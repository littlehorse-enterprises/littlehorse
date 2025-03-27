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
                });
            // Do some work that takes a while
            wf.SleepSeconds(30);
            wf.Execute("my-task");
        }
        
        return new Workflow("example-interrupt-handler", MyEntryPoint);
    }

    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            var client = config.GetGrpcClientInstance();
            var taskWorkers = GetTaskWorkers(config);
            foreach (var worker in taskWorkers)
            {
                worker.RegisterTaskDef();
            }
            
            var workflow = GetWorkflow();
            
            // Register external event if it does not exist
            HashSet<string> externalEventNames = workflow.GetRequiredExternalEventDefNames();

            foreach (var externalEventName in externalEventNames)
            {
                Console.WriteLine($"Registering external event {externalEventName}");
            
                client.PutExternalEventDef(new PutExternalEventDefRequest { Name = externalEventName });
            }
            
            workflow.RegisterWfSpec(config.GetGrpcClientInstance());
            
            Thread.Sleep(300);
            
            foreach (var worker in taskWorkers)
            {
                worker.Start();
            }
        }
    }
}