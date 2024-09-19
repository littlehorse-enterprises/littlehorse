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
        var props = new Dictionary<string, string>
        {
            { "AppSettings:Setting1", "Value1" }
        };

        IConfiguration configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(props)
            .Build();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();

            var config = new LHConfig(configuration, loggerFactory);

            MyWorker executable = new MyWorker();
            var taskWorker = new LHTaskWorker<MyWorker>(executable, "greet-dotnet", config);

            taskWorker.RegisterTaskDef();

            Thread.Sleep(1000);

            taskWorker.Start();
        }
    }
}
