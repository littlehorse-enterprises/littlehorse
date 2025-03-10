using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using SpawnThreadForEachExample;

namespace ConditionalsExample;

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
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            WfRunVariable parentVar = wf.DeclareJsonObj("approval-chain");
            parentVar.Assign(wf.Execute("parent-task-1", parentVar));
            SpawnedThread childThread = wf.SpawnThread("spawned-thread", 
                child =>
                {
                    var childVar = child.DeclareInt("child-var");
                    child.Execute("child-task", childVar);
                },
                new Dictionary<string, object>
                {
                    {
                        "child-var", parentVar
                    }
                });
            wf.WaitForThreads(SpawnedThreads.Of(childThread));

            wf.Execute("parent-task-2");
        }
        
        return new Workflow("spawn-parallel-threads-from-json-arr-variable", MyEntryPoint);
    }

    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            var client = config.GetGrpcClientInstance();
            var worker = new LHTaskWorker<MyWorker>(new MyWorker(), "parent-task-1", config);
            worker.RegisterTaskDef();

            var workflow = GetWorkflow();
            
            workflow.RegisterWfSpec(client);
            
            Thread.Sleep(300);

            worker.Start();
        }
    }
}