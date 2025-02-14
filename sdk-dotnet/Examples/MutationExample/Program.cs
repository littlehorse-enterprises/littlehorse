using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using MutationExample;

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

    private static LHConfig GetLHConfig(string[] args, ILoggerFactory loggerFactory)
    {
        var config = new LHConfig(loggerFactory);
        
        string filePath = Path.Combine(Directory.GetCurrentDirectory(), ".config/littlehorse.config");
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

    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            
            var executable = new MyWorker();
            var worker = new LHTaskWorker<MyWorker>(executable, TaskDefName, config);
            
            worker.RegisterTaskDef();
            
            var workflow = GetWorkflow();
            workflow.RegisterWfSpec(config.GetGrpcClientInstance());
            
            Thread.Sleep(300);
            
            worker.Start();
        }
    }
}