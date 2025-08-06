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
            WfRunVariable sleepTime = wf.DeclareInt("sleep-time").Required();
            wf.SleepSeconds(sleepTime);
            wf.ThrowEvent("sleep-done", "hello there!");
        }
        
        return new Workflow("await-wf-event", MyEntryPoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(loggerFactory);
            var client = config.GetGrpcClientInstance();

            await client.PutWorkflowEventDefAsync(
                new PutWorkflowEventDefRequest
                {
                    Name = "sleep-done"
                });
            
            await GetWorkflow().RegisterWfSpec(client);
            
            int delayMs = int.Parse(args[0]);
            string timeoutMs = args[1];
            int sleepSeconds = int.Parse(args[2]);
            
            await Task.Delay(1000);
            
            var id = Guid.NewGuid().ToString();
            
            Console.WriteLine($"Running workflow with id {id}");
            await client.RunWfAsync(new RunWfRequest
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
            await Task.Delay(delayMs);
            
            Console.WriteLine($"Now awaiting workflow event with timeout of {timeoutMs} milliseconds");
            
            var date = DateTime.UtcNow.AddMilliseconds(double.Parse(timeoutMs));
            var eventResult = await client.AwaitWorkflowEventAsync(new AwaitWorkflowEventRequest
                {
                    WfRunId = new WfRunId { Id = id }
                },
                deadline: date);
            
            Console.WriteLine($"Event: {eventResult}");
        }
    }
}