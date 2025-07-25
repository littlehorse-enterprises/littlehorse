﻿using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace MutationExample;

public abstract class Program
{
    const string TaskDefName = "spider-bite";
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

    private static LHConfig GetLHConfig(ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);
        var userProfilePath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        string filePath = Path.Combine(userProfilePath, ".config/littlehorse.config");
        
        if (File.Exists(filePath))
            config = new LHConfig(filePath, loggerFactory);

        return config;
    }
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            var theName = wf.DeclareStr("name");
            // We pass the name of the person and receive if it is spider-man or not
            NodeOutput output = wf.Execute(TaskDefName, theName);

            // We save the output in the variable
            wf.Mutate(theName, VariableMutationType.Assign, output);
        }
        
        return new Workflow("example-mutation", MyEntryPoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(loggerFactory);
            var executable = new MyWorker();
            var worker = new LHTaskWorker<MyWorker>(executable, TaskDefName, config);
            
            await worker.RegisterTaskDef();

            await GetWorkflow().RegisterWfSpec(config.GetGrpcClientInstance());

            await Task.Delay(300);

            await worker.Start();
        }
    }
}