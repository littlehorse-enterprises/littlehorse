using Grpc.Core;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Workflow.Spec;
using LittleHorse.Sdk.Worker;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;

namespace StructDefExample;

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
            WfRunVariable carInput = wf.DeclareStruct("car-input", typeof(ParkingTicketReport)).Required();
            WfRunVariable carOwner = wf.DeclareStruct("car-owner", typeof(Person));

            carOwner.Assign(wf.Execute("get-car-owner", carInput));
            wf.Execute("mail-ticket", carOwner);
        }

        return new Workflow("issue-parking-ticket", MyEntryPoint);
    }

    private static List<LHTaskWorker<MyWorker>> GetTaskWorkers(LHConfig config)
    {
        var worker = new MyWorker();
        return new List<LHTaskWorker<MyWorker>>
        {
            new LHTaskWorker<MyWorker>(worker, "get-car-owner", config),
            new LHTaskWorker<MyWorker>(worker, "mail-ticket", config)
        };
    }

    private static async Task RegisterStructDefs(LittleHorseClient client, ILogger logger)
    {
        var types = new[] { typeof(Address), typeof(Person), typeof(ParkingTicketReport) };

        foreach (var type in types)
        {
            var structDefType = new LHStructDefType(type);
            var request = new PutStructDefRequest
            {
                Name = structDefType.GetStructDefId().Name,
                Description = structDefType.GetStructDefDescription(),
                StructDef = structDefType.GetInlineStructDef(),
                AllowedUpdates = StructDefCompatibilityType.NoSchemaUpdates
            };

            try
            {
                var response = await client.PutStructDefAsync(request);
                logger.LogInformation("Created StructDef:\n{StructDef}", LHMappingHelper.ProtoToJson(response));
            }
            catch (RpcException ex) when (ex.StatusCode == StatusCode.AlreadyExists)
            {
                logger.LogInformation("StructDef {StructDefName} already exists.", request.Name);
            }
        }
    }

    public static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider == null)
        {
            return;
        }

        var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
        var logger = loggerFactory.CreateLogger<Program>();
        var config = GetLHConfig(loggerFactory);

        if (args.Length == 0)
        {
            await RunWorkers(config, logger);
        }
        else
        {
            await RunWorkflow(config, args, logger);
        }
    }

    private static async Task RunWorkers(LHConfig config, ILogger logger)
    {
        var client = config.GetGrpcClientInstance();
        var workflow = GetWorkflow();

        await RegisterStructDefs(client, logger);

        var workers = GetTaskWorkers(config);

        foreach (var worker in workers)
        {
            await worker.RegisterTaskDef();
        }

        await workflow.RegisterWfSpec(client);

        Console.CancelKeyPress += (_, _) =>
        {
            foreach (var worker in workers)
            {
                worker.Close();
            }
        };

        foreach (var worker in workers)
        {
            await worker.Start();
        }
    }

    private static async Task RunWorkflow(LHConfig config, string[] args, ILogger logger)
    {
        if (args.Length < 3)
        {
            logger.LogError("Expected 3 args: vehicleMake vehicleModel licensePlateNumber");
            return;
        }

        var client = config.GetGrpcClientInstance();
        string vehicleMake = args[0];
        string vehicleModel = args[1];
        string licensePlateNumber = args[2];

        var report = new ParkingTicketReport(vehicleMake, vehicleModel, licensePlateNumber, DateTime.UtcNow);

        logger.LogInformation("Generated parking ticket report from arguments:\n{Report}", report);

        await client.RunWfAsync(new RunWfRequest
        {
            WfSpecName = "issue-parking-ticket",
            Variables =
            {
                { "car-input", LHMappingHelper.ObjectToVariableValue(report) }
            }
        });
    }
}
