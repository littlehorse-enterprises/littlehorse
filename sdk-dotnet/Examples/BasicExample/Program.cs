using Examples.BasicExample;
using Microsoft.Extensions.Configuration;
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
    }
    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = new LHConfig(loggerFactory);

            MyWorker executable = new MyWorker();
            var taskWorker = new LHTaskWorker<MyWorker>(executable, "greet-dotnet", config);

            taskWorker.RegisterTaskDef();

            Thread.Sleep(1000);

            taskWorker.Start();
        }
    }
}
