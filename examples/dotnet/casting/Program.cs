using Examples.Casting;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace CastingExample;

public class Program
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
        var userProfilePath = Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
        string filePath = Path.Combine(userProfilePath, ".config/littlehorse.config");

        if (File.Exists(filePath))
            config = new LHConfig(filePath, loggerFactory);

        if (args.Length > 0)
        {
            var keyValueArgs = args.Select(item => item.Split('='))
                .ToDictionary(item => item[0], item => item[1]);
            config = new LHConfig(keyValueArgs, loggerFactory);
        }

        return config;
    }

    private static Workflow GetWorkflow()
    {
        void Entrypoint(WorkflowThread wf)
        {
            var stringInput = wf.DeclareStr("string-number").WithDefault("3.14");
            var stringBool = wf.DeclareStr("string-bool").WithDefault("false");
            var jsonInput = wf.DeclareJsonObj("json-input")
                .WithDefault(new Dictionary<string, object> { { "int", "1" }, { "string", "hello" } });

            // Cast STR -> DOUBLE for the task that expects a DOUBLE
            var doubleResult = wf.Execute("double-method", stringInput.CastTo(VariableType.Double));

            // Cast DOUBLE node output -> INT
            wf.Execute("int-method", doubleResult.CastToInt());

            // Arithmetic on a cast, then cast again to INT
            var mathResult = doubleResult.CastToDouble().Multiply(2.0).Divide(6.0);
            wf.Execute("int-method", mathResult.CastToInt());

            // Cast STR -> BOOL
            wf.Execute("bool-method", stringBool.CastToBool());

            // JsonPath + cast: extract an unknown-type field and cast to INT
            wf.Execute("int-method", jsonInput.WithJsonPath("$.int").CastToInt());
        }

        return new Workflow("casting-workflow", Entrypoint);
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);

            var worker1 = new LHTaskWorker<MyWorker>(new MyWorker(), "double-method", config);
            var worker2 = new LHTaskWorker<MyWorker>(new MyWorker(), "int-method", config);
            var worker3 = new LHTaskWorker<MyWorker>(new MyWorker(), "bool-method", config);
            var worker4 = new LHTaskWorker<MyWorker>(new MyWorker(), "string-method", config);

            await worker1.RegisterTaskDef();
            await worker2.RegisterTaskDef();
            await worker3.RegisterTaskDef();
            await worker4.RegisterTaskDef();

            await GetWorkflow().RegisterWfSpec(config.GetGrpcClientInstance());

            await Task.Delay(300);

            await Task.WhenAll(
                worker1.Start(),
                worker2.Start(),
                worker3.Start(),
                worker4.Start()
            );
        }
    }
}
