using Examples.BasicExample;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace BasicExample;

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
    
    private static Workflow GetChild()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            // In the `hierarchical-workflow` example, we require the variable to be INHERITED;
            // however, here the variable is an input.
            WfRunVariable childInputName = wf.DeclareStr("child-input-name").Required();
            wf.Complete();
        }
        
        return new Workflow("some-other-wfspec", MyEntryPoint);
    }

    private static Workflow GetParent()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            var theName = wf.DeclareStr("input-name").Required();
            var childOutput = wf.DeclareStr("child-output");

            var wfInput = new Dictionary<string, object>
            {
                { "child-input-name", theName },
            };
            SpawnedChildWf child = wf.RunWf("some-other-wfspec", wfInput);
            wf.Execute("greet", "hi from parent");

            childOutput.Assign(wf.WaitForChildWf(child));
        }
        
        return new Workflow("my-parent", MyEntryPoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            var worker = new LHTaskWorker<MyWorker>(new MyWorker(), "greet", config);

            await worker.RegisterTaskDef();

            await GetChild().RegisterWfSpec(config.GetGrpcClientInstance());
            await GetParent().RegisterWfSpec(config.GetGrpcClientInstance());

            await Task.Delay(300);

            await worker.Start();
        }
    }
}