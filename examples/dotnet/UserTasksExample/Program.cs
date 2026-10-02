using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.UserTask;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Worker;
using LittleHorse.Sdk.Workflow.Spec;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;

namespace UserTasksExample;

public abstract class Program
{
    private static ServiceProvider? _serviceProvider;
    
    private static readonly string WorkflowName = "it-request";
    public static readonly string EmailTaskName = "send-email";

    private static readonly string ItRequestForm = "it-request";
    public static readonly string ApprovalForm = "approve-it-request";
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
            WfRunVariable userId = wf.DeclareStr("user-id");
            WfRunVariable itRequest = wf.DeclareStruct("it-request", typeof(ItemRequestForm));
            WfRunVariable isApproved = wf.DeclareBool("is-approved");
            // Get the IT Request
            UserTaskOutput formOutput = wf.AssignUserTask(
                ItRequestForm,
                userId,
                "testGroup"
            );
            wf.ReleaseToGroupOnDeadline(formOutput, 60);
            
            wf.HandleAnyFailure(
                formOutput,
                handler => {
                    string email = "test-ut-support@gmail.com";
                    handler.Execute(EmailTaskName, email, "Task cancelled");
                }
            );
            itRequest.Assign(formOutput);

            // Have Finance approve the request
            UserTaskOutput financeUserTaskOutput = wf
                .AssignUserTask(ApprovalForm, null, "finance")
                .WithNotes(
                    wf.Format(
                        "User {0} is requesting to buy item {1}.\nJustification: {2}",
                        userId,
                        itRequest.Get("requestedItem"),
                        itRequest.Get("justification")
                    )
                );
            String financeTeamEmailBody = "Hi finance team, you have a new assigned task";
            String financeTeamEmail = "finance@gmail.com";
            wf.ScheduleReminderTask(
                financeUserTaskOutput,
                2,
                EmailTaskName,
                financeTeamEmail,
                financeTeamEmailBody
            );
            wf.ReassignUserTask(
                financeUserTaskOutput,
                "test-eduwer",
                null,
                60
            );

            isApproved.Assign(financeUserTaskOutput.Get("isApproved"));

            wf.DoIf(
                isApproved.IsEqualTo(true),
                // Request approved!
                ifBody => {
                    ifBody.Execute(
                        EmailTaskName,
                        userId,
                        wf.Format(
                            "Dear {0}, your request for {1} has been approved!",
                            userId,
                            itRequest.Get("requestedItem")
                        )
                    );
                }).DoElse(elseBody => {
                    elseBody.Execute(
                        EmailTaskName,
                        userId,
                        wf.Format(
                            "Dear {0}, your request for {1} has been denied.",
                            userId,
                            itRequest.Get("requestedItem")
                        )
                    );
                });
        }
        
        return new Workflow(WorkflowName, MyEntryPoint);
    }

    private static PutStructDefRequest CreateStructDef(Type type)
    {
        var schema = new LHStructDefType(type);
        return new PutStructDefRequest
        {
            Name = schema.GetStructDefId().Name,
            Description = schema.GetStructDefDescription(),
            StructDef = schema.GetInlineStructDef(),
            AllowedUpdates = StructDefCompatibilityType.NoSchemaUpdates
        };
    }

    static async Task Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(loggerFactory);
            var client = config.GetGrpcClientInstance();
            var worker = new LHTaskWorker<EmailSender>(new EmailSender(), "send-email", config);

            await worker.RegisterTaskDef();
            
            var requestStruct = await client.PutStructDefAsync(CreateStructDef(typeof(ItemRequestForm)));
            var approvalStruct = await client.PutStructDefAsync(CreateStructDef(typeof(ApprovalForm)));

            // Register the UserTaskDefs with the resolved minimum StructDef versions.
            UserTaskSchema requestForm = new UserTaskSchema(
                requestStruct.Id,
                ItRequestForm
            );
            await client.PutUserTaskDefAsync(requestForm.Compile());

            UserTaskSchema approvalForm = new UserTaskSchema(
                approvalStruct.Id,
                ApprovalForm
            );
            await client.PutUserTaskDefAsync(approvalForm.Compile());

            await GetWorkflow().RegisterWfSpec(client);

            Console.CancelKeyPress += (_, _) => worker.Close();

            await worker.Start();
        }
    }
}