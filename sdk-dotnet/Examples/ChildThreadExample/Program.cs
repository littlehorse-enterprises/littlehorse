using ChildThreadExample;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
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
            new(executable, "save-request-form-data", config),
            new(executable, "validate-identification", config),
            new(executable, "send-otp-by-email", config),
            new(executable, "send-otp-by-sms", config),
            new(executable, "check-otp", config),
            new(executable, "choose-option", config),
            new(executable, "fill-client-information-zendesk", config),
            new(executable, "create-ticket", config)
        };
        
        return workers;
    }
    
    private static Workflow GetWorkflow()
    {
        void MyEntryPoint(WorkflowThread wf)
        {
            var request = wf.DeclareJsonObj("request-data");
            NodeOutput customerSupportRequest = wf.WaitForEvent("customer-support-request");

            WfRunVariable channel = wf.DeclareStr("channel");
            channel.Assign(customerSupportRequest.WithJsonPath("$.channel"));
            wf.DoIf(wf.Condition(channel, Comparator.Equals, "WEBPAGE"),
                ifThread =>
                    ifThread.Execute("save-request-form-data"),
                elseThread => elseThread.DoIf(wf.Condition(channel, Comparator.Equals, "WHATSAPP"),
                    ifThread =>
                    {
                        ifThread.Execute("validate-identification");
                        var sendOtpByEmailThread = ifThread.SpawnThread(subThread => subThread.Execute("send-otp-by-email"),
                            "otp-by-email", null);
                        var sendOtpBySmsThread = ifThread.SpawnThread(subThread => subThread.Execute("send-otp-by-sms"),
                            "otp-by-sms", null);
                        ifThread.WaitForThreads(SpawnedThreads.Of(sendOtpByEmailThread, sendOtpBySmsThread));
                        ifThread.Execute("check-otp");
                        ifThread.Execute("choose-option");
                    },
                    elseThread => elseThread.DoIf(wf.Condition(channel, Comparator.Equals, "CALLCENTER"), 
                        ifThread => ifThread.Execute("fill-client-information-zendesk")
                        )
                    )
            );
            wf.Execute("create-ticket");

        }
        
        return new Workflow("example-customer-support-requests", MyEntryPoint);
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
            
            // Register external event if it does not exist
            HashSet<string> externalEventNames = workflow.GetRequiredExternalEventDefNames();

            foreach (var externalEventName in externalEventNames)
            {
                Console.WriteLine($"Registering external event {externalEventName}");
            
                client.PutExternalEventDef(new PutExternalEventDefRequest { Name = externalEventName });
            }
            
            workflow.RegisterWfSpec(client);
            
            Thread.Sleep(300);

            foreach (var worker in workers)
            {
                worker.Start();
            }
        }
    }
}