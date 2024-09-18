using Examples.BasicExample;
using Google.Protobuf;
using Google.Protobuf.WellKnownTypes;
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
                config.AddConsole();  // Add logging to the console
                config.SetMinimumLevel(LogLevel.Debug);  // Set minimum log level
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
        var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();

        var config = new LHConfig(configuration, loggerFactory);

        MyWorker executable = new MyWorker();
        var taskWorker = new LHTaskWorker<MyWorker>(executable, "greet-dotnet", config);

        taskWorker.RegisterTaskDef();

        Thread.Sleep(1000);

        taskWorker.Start();
    }
}
