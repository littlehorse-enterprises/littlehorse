using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

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
    
    private static List<LHTaskWorker<MyWorker>> GetTaskWorkers(LHConfig config)
    {
        MyWorker executable = new MyWorker();
        var workers = new List<LHTaskWorker<MyWorker>>
        {
            new(executable, "task-a", config),
            new(executable, "task-b", config),
            new(executable, "task-c", config),
            new(executable, "task-d", config)
        };
        
        return workers;
    }
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            var foo = wf.DeclareJsonObj("foo");
            wf.Execute("task-a");
            wf.DoIf(
                wf.Condition(foo.WithJsonPath("$.bar"), Comparator.GreaterThan, 10),
                ifThread => ifThread.Execute("task-b")
            ).DoElse(elseThread => elseThread.Execute("task-c"));
            wf.Execute("task-d");
        }
        
        return new Workflow("example-conditionals", MyEntryPoint);
    }

    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            var workers = GetTaskWorkers(config);
            foreach (var worker in workers)
            {
                worker.RegisterTaskDef();
            }

            var workflow = GetWorkflow();
            workflow.RegisterWfSpec(config.GetGrpcClientInstance());
            
            Thread.Sleep(300);
            var tasks = new List<Task>();
            foreach (var worker in workers)
            {
                tasks.Add(worker.Start());
            }

            Task.WaitAll(tasks.ToArray());
        }
    }
}