using Examples.BasicExample;
using Common.Configuration.Extension;
using LittleHorse.Common.Configuration;
using LittleHorse.Common.Configuration.Implementations;
using LittleHorse.Worker;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;

var builder = Host.CreateDefaultBuilder(args);

builder.ConfigureLHWorker(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile), ".config", "littlehorse.config"));

builder.ConfigureLogging((hostingContext, logging) =>
{
    logging.AddConsole();
    logging.SetMinimumLevel(LogLevel.Debug);
});

builder.ConfigureServices((hostingContext, services) =>
{
    services.AddSingleton<ILHWorkerConfig, LHWorkerConfig>();
    services.AddSingleton(provider =>
    {
        var logger = provider.GetService<ILogger<MyWorker>>();
        return new MyWorker(logger);
    }
    );
    services.AddSingleton(provider =>
    {
        var myWorker = provider.GetRequiredService<MyWorker>();
        var config = provider.GetRequiredService<ILHWorkerConfig>();
        var logger = provider.GetService<ILogger<LHTaskWorker<MyWorker>>>();

        return new LHTaskWorker<MyWorker>(myWorker, "greet", config, logger);
    });
});

var host = builder.Build();

var taskWorker = host.Services.GetRequiredService<LHTaskWorker<MyWorker>>();

if (!taskWorker.TaskDefExists())
{
    taskWorker.RegisterTaskDef();
}

taskWorker.Start();
