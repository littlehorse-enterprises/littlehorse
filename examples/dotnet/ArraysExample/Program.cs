using Examples.ArraysExample;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Workflow.Spec;
using LittleHorse.Sdk.Worker;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace ArraysExample;

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
        return args.Select(item => item.Split('='))
            .ToDictionary(item => item[0], item => item[1]);
    }

    private static LHConfig GetLHConfig(string[] args, ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);
        var userProfilePath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        string filePath = Path.Combine(userProfilePath, ".config/littlehorse.config");

        if (File.Exists(filePath))
        {
            config = new LHConfig(filePath, loggerFactory);
        }

        if (args.Length > 0)
        {
            var lhConfigs = GetDictionaryFromMainArgs(args);
            config = new LHConfig(lhConfigs, loggerFactory);
        }

        return config;
    }

    private static Workflow GetWorkflow()
    {
        void Entrypoint(WorkflowThread wf)
        {
            // Declare a typed LH Array variable (elements are Long).
            WfRunVariable arrVar = wf.DeclareArray("my-array", typeof(long));

            NodeOutput produced = wf.Execute("produce-array");
            arrVar.Assign(produced);

            wf.Execute("consume-array", arrVar);
        }

        return new Workflow("example-arrays", Entrypoint);
    }

    private static List<LHTaskWorker<ArrayWorker>> GetWorkers(LHConfig config)
    {
        var worker = new ArrayWorker();

        return new List<LHTaskWorker<ArrayWorker>>
        {
            new LHTaskWorker<ArrayWorker>(worker, "produce-array", config),
            new LHTaskWorker<ArrayWorker>(worker, "consume-array", config)
        };
    }

    public static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider == null)
        {
            return;
        }

        var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
        var logger = loggerFactory.CreateLogger<Program>();

        LHConfig config = GetLHConfig(args, loggerFactory);
        Workflow workflow = GetWorkflow();
        List<LHTaskWorker<ArrayWorker>> workers = GetWorkers(config);

        // Register TaskDefs.
        foreach (var worker in workers)
        {
            await worker.RegisterTaskDef();
        }

        // Register workflow.
        await workflow.RegisterWfSpec(config.GetGrpcClientInstance());

        await Task.Delay(300);

        // Start workers.
        foreach (var worker in workers)
        {
            logger.LogInformation("Starting worker {TaskDefName}", worker.TaskDefName);
        }

        await Task.WhenAll(workers.Select(worker => worker.Start()));
    }
}
