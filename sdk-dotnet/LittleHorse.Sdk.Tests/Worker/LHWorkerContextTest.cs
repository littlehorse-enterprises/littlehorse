using System;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Worker;
using Moq;
using Xunit;
using static LittleHorse.Sdk.Common.Proto.LittleHorse;
using grpc = global::Grpc.Core;
public class LHWorkerContextTest
{
    public LHWorkerContextTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
    }
    
    [Fact]
    public void WorkerContesxt_ShouldPutTaskCheckpointOnFirstCheckpointAttempt()
    {
        var scheduledTask = new ScheduledTask()
        {
          TaskRunId = new TaskRunId
          {
            WfRunId = new WfRunId
            {
              Id = "mock-wf"
            },
            TaskGuid = "mock-guid"
          },
          TotalObservedCheckpoints = 0
        };

        var mockClient = new Mock<LittleHorseClient>();
        mockClient.Setup(m =>  m.PutCheckpoint(
          It.IsAny<PutCheckpointRequest>(), 
          It.IsAny<grpc::Metadata>(), 
          It.IsAny<System.DateTime?>(), 
          It.IsAny<System.Threading.CancellationToken>())).Returns(new PutCheckpointResponse
          {
            FlowControlContinueType = PutCheckpointResponse.Types.FlowControlContinue.ContinueTask
          });

        var workerContext = new LHWorkerContext(scheduledTask, new DateTime(), mockClient.Object);

        var checkpointData = workerContext.ExecuteAndCheckpoint<string>((LHCheckpointContext) =>
        {
          return "checkpointValue";
        });

        mockClient.Verify(m => m.PutCheckpoint(
          It.IsAny<PutCheckpointRequest>(), 
          It.IsAny<grpc::Metadata>(), 
          It.IsAny<DateTime?>(), 
          It.IsAny<System.Threading.CancellationToken>()), Times.Once);
    }

        [Fact]
    public void WorkerContesxt_ShouldGetCheckpointOnSecondCheckpointAttempt()
    {
        var taskRunId = new TaskRunId
          {
            WfRunId = new WfRunId
            {
              Id = "mock-wf"
            },
            TaskGuid = "mock-guid"
          };
        
        var scheduledTask = new ScheduledTask()
        {
          TaskRunId = taskRunId,
          TotalObservedCheckpoints = 1
        };

        var mockClient = new Mock<LittleHorseClient>();
        mockClient.Setup(m =>  m.GetCheckpoint(
          It.IsAny<CheckpointId>(), 
          It.IsAny<grpc::Metadata>(), 
          It.IsAny<DateTime?>(), 
          It.IsAny<System.Threading.CancellationToken>())).Returns(new Checkpoint
          {
            Id = new CheckpointId
            {
              TaskRun = taskRunId,
              CheckpointNumber = 1
            },
            Value = new VariableValue
            {
              Str = "checkpointValue"
            }
          });

        var workerContext = new LHWorkerContext(scheduledTask, new DateTime(), mockClient.Object);

        var checkpointData = workerContext.ExecuteAndCheckpoint<string>((LHCheckpointContext) =>
        {
          return "checkpointValue";
        });

        mockClient.Verify(m => m.PutCheckpoint(
          It.IsAny<PutCheckpointRequest>(), 
          It.IsAny<grpc::Metadata>(), 
          It.IsAny<DateTime?>(), 
          It.IsAny<System.Threading.CancellationToken>()), Times.Never);
        mockClient.Verify(m => m.GetCheckpoint(
          It.IsAny<CheckpointId>(), 
          It.IsAny<grpc::Metadata>(), 
          It.IsAny<DateTime?>(), 
          It.IsAny<System.Threading.CancellationToken>()), Times.Once);
    }
}