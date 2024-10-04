using Examples.BasicExample;
using LittleHorse.Sdk;
using LittleHorse.Worker;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.DependencyInjection;

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
    } static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = new LHConfig(loggerFactory);
            string filePath = Path.Combine(Directory.GetCurrentDirectory(), ".config/littlehorse.config");
            if (File.Exists(filePath))
                config = new LHConfig(filePath, loggerFactory);

            MyWorker executable = new MyWorker();
            var taskWorker = new LHTaskWorker<MyWorker>(executable, "greet-dotnet", config);

            taskWorker.RegisterTaskDef();

            Thread.Sleep(1000);

            taskWorker.Start();
        }
    }
}
