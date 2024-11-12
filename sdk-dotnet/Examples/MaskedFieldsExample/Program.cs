using Examples.BasicExample;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;

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

    private static LHConfig GetLHConfig(string[] args, ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);
        
        string filePath = Path.Combine(Directory.GetCurrentDirectory(), ".config/littlehorse.config");
        if (File.Exists(filePath))
            config = new LHConfig(filePath, loggerFactory);

        return config;
    }

    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            
            MyWorker executableCreateGreet = new MyWorker();
            var taskWorkerCreate = new LHTaskWorker<MyWorker>(executableCreateGreet, "create-greet", config);
            MyWorker executableUpdateGreet = new MyWorker();
            var taskWorkerUpdate = new LHTaskWorker<MyWorker>(executableUpdateGreet, "update-greet", config);
            MyWorker executableDeleteGreet = new MyWorker();
            var taskWorkerDelete = new LHTaskWorker<MyWorker>(executableDeleteGreet, "delete-greet", config);

            taskWorkerCreate.RegisterTaskDef();
            taskWorkerUpdate.RegisterTaskDef();
            taskWorkerDelete.RegisterTaskDef();

            Thread.Sleep(1000);

            taskWorkerCreate.Start();
            taskWorkerUpdate.Start();
            taskWorkerDelete.Start();
        }
    }
}
