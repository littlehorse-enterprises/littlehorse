using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace AwaitWorkflowEventExample;

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
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            WfRunVariable sleepTime = wf.DeclareInt("sleep-time").Required();
            wf.SleepSeconds(sleepTime);
            wf.ThrowEvent("sleep-done", "hello there!");
        }
        
        return new Workflow("await-wf-event", MyEntryPoint);
    }

    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            var client = config.GetGrpcClientInstance();
            
            var workflow = GetWorkflow();
            client.PutWorkflowEventDef(
                new PutWorkflowEventDefRequest
                {
                    Name = "sleep-done"
                });
            
            workflow.RegisterWfSpec(client);
            
            int delayMs = int.Parse(args[0]);
            string timeoutMs = args[1];
            int sleepSeconds = int.Parse(args[2]);
            
            Thread.Sleep(1000);
            
            var id = Guid.NewGuid().ToString();
            
            Console.WriteLine($"Running workflow with id {id}");
            client.RunWf(new RunWfRequest
            {
                Id = id,
                WfSpecName = "await-wf-event",
                Variables =
                {
                    {
                        "sleep-time", LHMappingHelper.ObjectToVariableValue(sleepSeconds)
                    }
                }
            });
            Console.WriteLine($"Sleeping for {delayMs} milliseconds");
            Thread.Sleep(delayMs);
            
            Console.WriteLine($"Now awaiting workflow event with timeout of {timeoutMs} milliseconds");
            
            var date = DateTime.UtcNow.AddMilliseconds(double.Parse(timeoutMs));
            var eventResult = client.AwaitWorkflowEvent(new AwaitWorkflowEventRequest
                {
                    WfRunId = new WfRunId { Id = id }
                },
                deadline: date);
            
            Console.WriteLine($"Event: {eventResult}");
        }
    }
}