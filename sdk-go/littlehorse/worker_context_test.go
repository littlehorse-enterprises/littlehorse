package littlehorse

import (
	"context"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"google.golang.org/grpc"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// mockLittleHorseClient is a mock implementation of the LittleHorseClient for testing
type mockLittleHorseClient struct {
	lhproto.LittleHorseClient
	putCheckpointCalled bool
	getCheckpointCalled bool
	checkpointValue     *lhproto.VariableValue
	flowControl         lhproto.PutCheckpointResponse_FlowControlContinue
}

func (m *mockLittleHorseClient) PutCheckpoint(ctx context.Context, in *lhproto.PutCheckpointRequest, opts ...grpc.CallOption) (*lhproto.PutCheckpointResponse, error) {
	m.putCheckpointCalled = true
	m.checkpointValue = in.Value
	return &lhproto.PutCheckpointResponse{
		FlowControlContinueType: m.flowControl,
	}, nil
}

func (m *mockLittleHorseClient) GetCheckpoint(ctx context.Context, in *lhproto.CheckpointId, opts ...grpc.CallOption) (*lhproto.Checkpoint, error) {
	m.getCheckpointCalled = true
	return &lhproto.Checkpoint{
		Id:    in,
		Value: m.checkpointValue,
	}, nil
}

func TestExecuteAndCheckpoint_FirstAttempt_ShouldPutCheckpoint(t *testing.T) {
	// Setup
	mockClient := &mockLittleHorseClient{
		flowControl: lhproto.PutCheckpointResponse_CONTINUE_TASK,
	}
	var client lhproto.LittleHorseClient = mockClient

	scheduledTask := &lhproto.ScheduledTask{
		TaskRunId: &lhproto.TaskRunId{
			WfRunId: &lhproto.WfRunId{
				Id: "test-wf-run",
			},
			TaskGuid: "test-task-guid",
		},
		TotalObservedCheckpoints: 0, // No previous checkpoints
		AttemptNumber:            0,
	}

	workerContext := newWorkerContext(
		scheduledTask,
		timestamppb.Now(),
		&client,
	)

	// Execute
	checkpointFunc := func(ctx *CheckpointContext) (interface{}, error) {
		ctx.Log("test log")
		return "checkpoint-value", nil
	}

	result, err := workerContext.ExecuteAndCheckpoint(checkpointFunc)

	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if result != "checkpoint-value" {
		t.Errorf("Expected result to be 'checkpoint-value', got %v", result)
	}

	if !mockClient.putCheckpointCalled {
		t.Error("Expected PutCheckpoint to be called")
	}

	if mockClient.getCheckpointCalled {
		t.Error("Expected GetCheckpoint NOT to be called on first attempt")
	}
}

func TestExecuteAndCheckpoint_RetryAttempt_ShouldGetCheckpoint(t *testing.T) {
	// Setup
	expectedValue := &lhproto.VariableValue{
		Value: &lhproto.VariableValue_Str{
			Str: "saved-checkpoint-value",
		},
	}

	mockClient := &mockLittleHorseClient{
		checkpointValue: expectedValue,
		flowControl:     lhproto.PutCheckpointResponse_CONTINUE_TASK,
	}
	var client lhproto.LittleHorseClient = mockClient

	scheduledTask := &lhproto.ScheduledTask{
		TaskRunId: &lhproto.TaskRunId{
			WfRunId: &lhproto.WfRunId{
				Id: "test-wf-run",
			},
			TaskGuid: "test-task-guid",
		},
		TotalObservedCheckpoints: 1, // One previous checkpoint
		AttemptNumber:            1,
	}

	workerContext := newWorkerContext(
		scheduledTask,
		timestamppb.Now(),
		&client,
	)

	// Execute
	checkpointFunc := func(ctx *CheckpointContext) (interface{}, error) {
		// This should NOT be called since we have a previous checkpoint
		t.Error("Checkpoint function should not be called on retry with existing checkpoint")
		return "new-value", nil
	}

	result, err := workerContext.ExecuteAndCheckpoint(checkpointFunc)

	// Assert
	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	if result != "saved-checkpoint-value" {
		t.Errorf("Expected result to be 'saved-checkpoint-value', got %v", result)
	}

	if mockClient.putCheckpointCalled {
		t.Error("Expected PutCheckpoint NOT to be called on retry")
	}

	if !mockClient.getCheckpointCalled {
		t.Error("Expected GetCheckpoint to be called on retry")
	}
}

func TestExecuteAndCheckpoint_MultipleCheckpoints(t *testing.T) {
	// Setup
	mockClient := &mockLittleHorseClient{
		flowControl: lhproto.PutCheckpointResponse_CONTINUE_TASK,
	}
	var client lhproto.LittleHorseClient = mockClient

	scheduledTask := &lhproto.ScheduledTask{
		TaskRunId: &lhproto.TaskRunId{
			WfRunId: &lhproto.WfRunId{
				Id: "test-wf-run",
			},
			TaskGuid: "test-task-guid",
		},
		TotalObservedCheckpoints: 0, // No previous checkpoints
		AttemptNumber:            0,
	}

	workerContext := newWorkerContext(
		scheduledTask,
		timestamppb.Now(),
		&client,
	)

	// Execute first checkpoint
	result1, err := workerContext.ExecuteAndCheckpoint(func(ctx *CheckpointContext) (interface{}, error) {
		return "first-checkpoint", nil
	})

	if err != nil {
		t.Fatalf("Expected no error on first checkpoint, got %v", err)
	}

	if result1 != "first-checkpoint" {
		t.Errorf("Expected first result to be 'first-checkpoint', got %v", result1)
	}

	// Execute second checkpoint
	result2, err := workerContext.ExecuteAndCheckpoint(func(ctx *CheckpointContext) (interface{}, error) {
		return "second-checkpoint", nil
	})

	if err != nil {
		t.Fatalf("Expected no error on second checkpoint, got %v", err)
	}

	if result2 != "second-checkpoint" {
		t.Errorf("Expected second result to be 'second-checkpoint', got %v", result2)
	}

	// Verify checkpointsSoFarInThisRun is incremented correctly
	if workerContext.checkpointsSoFarInThisRun != 2 {
		t.Errorf("Expected checkpointsSoFarInThisRun to be 2, got %d", workerContext.checkpointsSoFarInThisRun)
	}
}

func TestExecuteAndCheckpoint_CheckpointContextLogging(t *testing.T) {
	// Setup
	mockClient := &mockLittleHorseClient{
		flowControl: lhproto.PutCheckpointResponse_CONTINUE_TASK,
	}
	var client lhproto.LittleHorseClient = mockClient

	scheduledTask := &lhproto.ScheduledTask{
		TaskRunId: &lhproto.TaskRunId{
			WfRunId: &lhproto.WfRunId{
				Id: "test-wf-run",
			},
			TaskGuid: "test-task-guid",
		},
		TotalObservedCheckpoints: 0,
		AttemptNumber:            0,
	}

	workerContext := newWorkerContext(
		scheduledTask,
		timestamppb.Now(),
		&client,
	)

	// Execute
	_, err := workerContext.ExecuteAndCheckpoint(func(ctx *CheckpointContext) (interface{}, error) {
		ctx.Log("test log message")
		ctx.Log(42)
		ctx.Log(nil)
		return "value", nil
	})

	if err != nil {
		t.Fatalf("Expected no error, got %v", err)
	}

	// Note: We can't directly check the logs sent to PutCheckpoint in this simple mock,
	// but we can verify that the checkpoint context logging works
}

func TestCheckpointContext_Log(t *testing.T) {
	ctx := newCheckpointContext()

	ctx.Log("hello")
	if ctx.getLogOutput() != "hello" {
		t.Errorf("Expected 'hello', got '%s'", ctx.getLogOutput())
	}

	ctx.Log(" world")
	if ctx.getLogOutput() != "hello world" {
		t.Errorf("Expected 'hello world', got '%s'", ctx.getLogOutput())
	}

	ctx.Log(nil)
	if ctx.getLogOutput() != "hello worldnil" {
		t.Errorf("Expected 'hello worldnil', got '%s'", ctx.getLogOutput())
	}

	ctx.Log(123)
	if ctx.getLogOutput() != "hello worldnil123" {
		t.Errorf("Expected 'hello worldnil123', got '%s'", ctx.getLogOutput())
	}
}
