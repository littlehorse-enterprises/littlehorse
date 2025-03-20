using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.UserTask;
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
            WfRunVariable userId = wf.DeclareStr("user-id");
            WfRunVariable itRequest = wf.DeclareJsonObj("it-request");
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
                        itRequest.WithJsonPath("$.RequestedItem"),
                        itRequest.WithJsonPath("$.Justification")
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

            isApproved.Assign(financeUserTaskOutput.WithJsonPath("$.IsApproved"));

            wf.DoIf(
                wf.Condition(isApproved, Comparator.Equals, true),
                // Request approved!
                ifBody => {
                    ifBody.Execute(
                        EmailTaskName,
                        userId,
                        wf.Format(
                            "Dear {0}, your request for {1} has been approved!",
                            userId,
                            itRequest.WithJsonPath("$.RequestedItem")
                        )
                    );
                },
                // Request denied ):
                elseBody => {
                    elseBody.Execute(
                        EmailTaskName,
                        userId,
                        wf.Format(
                            "Dear {0}, your request for {1} has been denied.",
                            userId,
                            itRequest.WithJsonPath("$.RequestedItem")
                        )
                    );
                }
            );
        }
        
        return new Workflow(WorkflowName, MyEntryPoint);
    }

    static void Main(string[] args)
    {
        SetupApplication();
        if (_serviceProvider != null)
        {
            var loggerFactory = _serviceProvider.GetRequiredService<ILoggerFactory>();
            var config = GetLHConfig(args, loggerFactory);
            var client = config.GetGrpcClientInstance();
            var worker = new LHTaskWorker<EmailSender>(new EmailSender(), "send-email", config);
            worker.RegisterTaskDef();
            
            // Create the User Task Def
            UserTaskSchema requestForm = new UserTaskSchema(
                new ItemRequestForm(),
                ItRequestForm
            );
            client.PutUserTaskDef(requestForm.Compile());

            UserTaskSchema approvalForm = new UserTaskSchema(
                new ApprovalForm(),
                ApprovalForm
            );
            client.PutUserTaskDef(approvalForm.Compile());

            var workflow = GetWorkflow();
            
            workflow.RegisterWfSpec(client);
            
            Thread.Sleep(300);

            worker.Start();
        }
    }
}