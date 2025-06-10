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

    private static LHConfig GetLHConfig(string[] args, ILoggerFactory loggerFactory)
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
            new(executable, "ask-for-name", config),
            new(executable, "greet", config)
        };
        
        return workers;
    }
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            WfRunVariable name = wf.DeclareStr("name").Searchable();
            wf.Execute("ask-for-name");
            name.Assign(wf.WaitForEvent("name-event"));
            wf.Execute("greet", name);
        }
        
        return new Workflow("example-external-event", MyEntryPoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            var client = config.GetGrpcClientInstance();
            var workers = GetTaskWorkers(config);

            await Task.WhenAll(workers.Select(worker => worker.RegisterTaskDef()));
            
            var workflow = GetWorkflow();
            
            // Register external event if it does not exist
            foreach (var externalEventName in workflow.GetRequiredExternalEventDefNames())
            {
                Console.WriteLine($"Registering external event {externalEventName}");
                client.PutExternalEventDef(new PutExternalEventDefRequest { Name = externalEventName });
            }
            
            await workflow.RegisterWfSpec(config.GetGrpcClientInstance());
            
            await Task.Delay(300);

            await Task.WhenAll(workers.Select(worker => worker.Start()));
        }
    }
}