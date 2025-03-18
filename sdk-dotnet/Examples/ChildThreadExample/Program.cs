﻿using ChildThreadExample;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace ConditionalsExample;

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
        MyWorker executable = new MyWorker();
        var workers = new List<LHTaskWorker<MyWorker>>
        {
            new(executable, "parent-task-1", config),
            new(executable, "child-task", config),
            new(executable, "parent-task-2", config)
        };
        
        return workers;
    }
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            var parentVar = wf.DeclareInt("parent-var");
            parentVar.Assign(wf.Execute("parent-task-1", parentVar));
            SpawnedThread childThread = wf.SpawnThread(
                "spawned-thread",
                child =>
                {
                    var childVar = child.DeclareInt("child-var");
                    child.Execute("child-task", childVar);
                },
                new Dictionary<string, object>
                {
                    {
                        "child-var", parentVar
                    }
                });
            wf.WaitForThreads(SpawnedThreads.Of(childThread));

            wf.Execute("parent-task-2");
        }
        
        return new Workflow("example-child-thread", MyEntryPoint);
    }

    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            var client = config.GetGrpcClientInstance();
            var workers = GetTaskWorkers(config);
            foreach (var worker in workers)
            {
                worker.RegisterTaskDef();
            }

            var workflow = GetWorkflow();
            
            workflow.RegisterWfSpec(client);
            
            Thread.Sleep(300);

            foreach (var worker in workers)
            {
                worker.Start();
            }
        }
    }
}