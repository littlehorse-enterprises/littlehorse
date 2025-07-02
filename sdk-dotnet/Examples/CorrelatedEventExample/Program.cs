using CorrelatedEventExample;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace CorrelatedEventExample;
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
            WfRunVariable documentId = wf.DeclareStr("document-id");
            wf.WaitForEvent("document-signed").WithCorrelationId(documentId);
        }

        return new Workflow("example-correlated-event", MyEntryPoint);
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

            // Register external event if it does not exist
            HashSet<string> externalEventNames = workflow.GetRequiredExternalEventDefNames();

            foreach (var externalEventName in externalEventNames)
            {
                Console.WriteLine($"Registering external event {externalEventName}");

                client.PutExternalEventDef(new PutExternalEventDefRequest { Name = externalEventName, CorrelatedEventConfig = new CorrelatedEventConfig() });
            }

            workflow.RegisterWfSpec(config.GetGrpcClientInstance());

            Thread.Sleep(300);

        }
    }
}