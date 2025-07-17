using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace ChildThreadsForeachExample;

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
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            WfRunVariable approvalChain = wf.DeclareJsonObj("approval-chain");
            SpawnedThreads spawnedThreads = wf.SpawnThreadForEach(approvalChain.WithJsonPath("$.approvals"),
                "spawn-threads",
                innerThread =>
                {
                    innerThread.DeclareInt("not-used-variable");
                    WfRunVariable inputVariable = innerThread.DeclareJsonObj(WorkflowThread.HandlerInputVar);
                    innerThread.Execute("task-executor", inputVariable.WithJsonPath("$.user"));
                },
                new Dictionary<string, object>
                {
                    {
                        "not-used-variable", 1234
                    }
                });
            wf.WaitForThreads(spawnedThreads);
            wf.Execute("task-executor", approvalChain.WithJsonPath("$.description"));
        }
        
        return new Workflow("spawn-parallel-threads-from-json-arr-variable", MyEntryPoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(loggerFactory);
            var client = config.GetGrpcClientInstance();
            var worker = new LHTaskWorker<MyWorker>(new MyWorker(), "task-executor", config);
            
            await worker.RegisterTaskDef();

            await GetWorkflow().RegisterWfSpec(client);
            
            await Task.Delay(300);

            await worker.Start();
        }
    }
}