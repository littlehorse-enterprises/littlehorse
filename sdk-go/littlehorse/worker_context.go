package littlehorse

import (
	"context"
	"encoding/json"
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// CheckpointableFunction represents a function that can be checkpointed.
// It accepts a CheckpointContext and returns a value and an error.
type CheckpointableFunction func(*CheckpointContext) (interface{}, error)

type WorkerContext struct {
	ScheduledTask             *lhproto.ScheduledTask
	ScheduleTime              *timestamppb.Timestamp
	stderr                    string
	checkpointsSoFarInThisRun int32
	client                    *lhproto.LittleHorseClient
}

func NewWorkerContext(
	scheduledTask *lhproto.ScheduledTask,
	scheduleTime *timestamppb.Timestamp,
) *WorkerContext {
	return &WorkerContext{
		ScheduledTask:             scheduledTask,
		ScheduleTime:              scheduleTime,
		checkpointsSoFarInThisRun: 0,
		client:                    nil,
	}
}

// NewWorkerContextWithClient creates a new WorkerContext with a gRPC client for checkpoint operations.
func NewWorkerContextWithClient(
	scheduledTask *lhproto.ScheduledTask,
	scheduleTime *timestamppb.Timestamp,
	client *lhproto.LittleHorseClient,
) *WorkerContext {
	return &WorkerContext{
		ScheduledTask:             scheduledTask,
		ScheduleTime:              scheduleTime,
		checkpointsSoFarInThisRun: 0,
		client:                    client,
	}
}

func (wc *WorkerContext) GetWfRunId() *lhproto.WfRunId {
	return GetWfRunIdFromTaskSource(wc.ScheduledTask.Source)
}

func (wc *WorkerContext) GetNodeRunId() *lhproto.NodeRunId {
	switch src := wc.ScheduledTask.Source.TaskRunSource.(type) {
	case *lhproto.TaskRunSource_TaskNode:
		return src.TaskNode.NodeRunId
	case *lhproto.TaskRunSource_UserTaskTrigger:
		return src.UserTaskTrigger.NodeRunId
	}
	return nil
}

func (wc *WorkerContext) GetAttemptNumber() int32 {
	return wc.ScheduledTask.GetAttemptNumber()
}

func (wc *WorkerContext) GetScheduledTime() *timestamppb.Timestamp {
	return wc.ScheduleTime
}

func (wc *WorkerContext) GetIdempotencyKey() string {
	return wc.ScheduledTask.TaskRunId.TaskGuid
}

func (wc *WorkerContext) Log(thing interface{}) {
	if thing != nil {
		wc.stderr += fmt.Sprint(thing)
	} else {
		wc.stderr += "nil"
	}
}

func (wc *WorkerContext) GetLogOutput() string {
	return wc.stderr
}

// ExecuteAndCheckpoint executes a checkpointable function and checkpoints the result with the server.
//
// On the first checkpoint attempt, the Task Worker will execute your CheckpointableFunction
// and put the result to the server as a Checkpoint.
//
// If your overall Task Attempt fails after your Checkpoint, this method will
// retrieve the Checkpoint from the server on future iterations.
//
// The function returns the result as interface{} or an error if the checkpoint operation fails.
// The caller should type-assert the result to the expected type.
func (wc *WorkerContext) ExecuteAndCheckpoint(fn CheckpointableFunction) (interface{}, error) {
	if wc.checkpointsSoFarInThisRun < wc.ScheduledTask.GetTotalObservedCheckpoints() {
		// Fetch checkpoint from server
		result, err := wc.fetchCheckpoint(wc.checkpointsSoFarInThisRun)
		wc.checkpointsSoFarInThisRun++
		return result, err
	} else {
		// Execute and save checkpoint
		return wc.saveCheckpoint(fn)
	}
}

// fetchCheckpoint retrieves a checkpoint from the server based on its checkpoint order number.
func (wc *WorkerContext) fetchCheckpoint(checkpointNumber int32) (interface{}, error) {
	if wc.client == nil {
		return nil, fmt.Errorf("client is not initialized for checkpoint operations")
	}

	id := &lhproto.CheckpointId{
		TaskRun:          wc.ScheduledTask.TaskRunId,
		CheckpointNumber: checkpointNumber,
	}

	checkpoint, err := (*wc.client).GetCheckpoint(context.Background(), id)
	if err != nil {
		return nil, fmt.Errorf("failed to get checkpoint: %w", err)
	}

	// Extract the value from VariableValue based on its type
	result, err := varValToInterface(checkpoint.Value)
	if err != nil {
		return nil, err
	}

	return result, nil
}

// varValToInterface converts a VariableValue to a Go interface{}.
// This is a helper function for checkpoint deserialization.
func varValToInterface(varVal *lhproto.VariableValue) (interface{}, error) {
	switch v := varVal.GetValue().(type) {
	case *lhproto.VariableValue_Int:
		return v.Int, nil
	case *lhproto.VariableValue_Double:
		return v.Double, nil
	case *lhproto.VariableValue_Bool:
		return v.Bool, nil
	case *lhproto.VariableValue_Str:
		return v.Str, nil
	case *lhproto.VariableValue_Bytes:
		return v.Bytes, nil
	case *lhproto.VariableValue_JsonArr:
		// For JSON arrays, we need to deserialize to a generic structure
		var arr interface{}
		err := json.Unmarshal([]byte(v.JsonArr), &arr)
		if err != nil {
			return nil, fmt.Errorf("failed to deserialize JSON array: %w", err)
		}
		return arr, nil
	case *lhproto.VariableValue_JsonObj:
		// For JSON objects, we need to deserialize to a generic structure
		var obj interface{}
		err := json.Unmarshal([]byte(v.JsonObj), &obj)
		if err != nil {
			return nil, fmt.Errorf("failed to deserialize JSON object: %w", err)
		}
		return obj, nil
	case nil:
		return nil, nil
	default:
		return nil, fmt.Errorf("unknown VariableValue type")
	}
}

// saveCheckpoint executes a checkpointable function and puts the result to the server.
func (wc *WorkerContext) saveCheckpoint(fn CheckpointableFunction) (interface{}, error) {
	if wc.client == nil {
		return nil, fmt.Errorf("client is not initialized for checkpoint operations")
	}

	checkpointContext := NewCheckpointContext()
	result, err := fn(checkpointContext)
	if err != nil {
		return nil, err
	}

	// Convert result to VariableValue
	varVal, err := InterfaceToVarVal(result)
	if err != nil {
		return nil, fmt.Errorf("failed to serialize checkpoint value: %w", err)
	}

	request := &lhproto.PutCheckpointRequest{
		TaskAttempt: wc.ScheduledTask.AttemptNumber,
		TaskRunId:   wc.ScheduledTask.TaskRunId,
		Value:       varVal,
	}

	// Add logs if any
	if checkpointContext.GetLogOutput() != "" {
		logs := checkpointContext.GetLogOutput()
		request.Logs = &logs
	}

	response, err := (*wc.client).PutCheckpoint(context.Background(), request)
	if err != nil {
		return nil, fmt.Errorf("failed to put checkpoint: %w", err)
	}

	wc.checkpointsSoFarInThisRun++

	if response.FlowControlContinueType != lhproto.PutCheckpointResponse_CONTINUE_TASK {
		return nil, fmt.Errorf("checkpoint operation halted by server flow control (received: %v)", response.FlowControlContinueType)
	}

	return result, nil
}
