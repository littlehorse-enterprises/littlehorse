﻿using Common.Configuration.Extension;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.DependencyInjection;
using LittleHorse.Common.Configuration;
using LittleHorse.Common.Configuration.Implementations;
using LittleHorse.Worker;
using BasicExampleConsoleApp;

var builder = Host.CreateDefaultBuilder(args);

builder.ConfigureLHWorker();

builder.ConfigureLogging((hostingContext, logging) =>
{
    logging.AddConsole();
    logging.SetMinimumLevel(LogLevel.Debug); 
});

builder.ConfigureServices((hostingContext, services) =>
{
    services.AddSingleton<ILHWorkerConfig, LHWorkerConfig>();
    services.AddSingleton<MyWorker>();
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

taskWorker.Start();

