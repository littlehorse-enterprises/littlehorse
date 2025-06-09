using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;

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
            new(executableExceptionHandling, "create-greet", config),
            new(executableExceptionHandling, "update-greet", config),
            new(executableExceptionHandling, "delete-greet", config)
        };
        
        return workers;
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