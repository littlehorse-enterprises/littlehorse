package littlehorse

import (
	"context"
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// CheckpointableFunction represents a function that can be checkpointed.
// It accepts a CheckpointContext and returns a value and an error.
type CheckpointableFunction func(*CheckpointContext) (interface{}, error)

// WorkerContext contains runtime information about the specific WfRun and NodeRun
// being executed by the Task Worker. It is automatically provided by the SDK when
// a task function includes it as a parameter.
//
// Note: WorkerContext instances are created internally by the SDK. Do not instantiate
// this struct directly.
type WorkerContext struct {
	ScheduledTask             *lhproto.ScheduledTask
	ScheduleTime              *timestamppb.Timestamp
	stderr                    string
	checkpointsSoFarInThisRun int32
	client                    *lhproto.LittleHorseClient
}

// CheckpointContext contains runtime information for a checkpoint execution.
// It provides logging capabilities that are specific to the checkpoint being executed.
type CheckpointContext struct {
	logOutput string
}

// newWorkerContext creates a new WorkerContext with a gRPC client.
// This is an internal function used by the task worker to provision WorkerContext instances.
func newWorkerContext(
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

// newCheckpointContext creates a new CheckpointContext instance.
func newCheckpointContext() *CheckpointContext {
	return &CheckpointContext{
		logOutput: "",
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
// The function returns the result as interface{} which should be type-asserted to the expected type.
// Example:
//
//	result, err := context.ExecuteAndCheckpoint(func(ctx *CheckpointContext) (interface{}, error) {
//	    return "hello world", nil
//	})
//	str := result.(string)
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
	id := &lhproto.CheckpointId{
		TaskRun:          wc.ScheduledTask.TaskRunId,
		CheckpointNumber: checkpointNumber,
	}

	checkpoint, err := (*wc.client).GetCheckpoint(context.Background(), id)
	if err != nil {
		return nil, fmt.Errorf("failed to get checkpoint: %w", err)
	}

	// Extract the value from VariableValue based on its type
	result, err := VarValToInterface(checkpoint.Value)
	if err != nil {
		return nil, err
	}

	return result, nil
}

// saveCheckpoint executes a checkpointable function and puts the result to the server.
func (wc *WorkerContext) saveCheckpoint(fn CheckpointableFunction) (interface{}, error) {
	checkpointContext := newCheckpointContext()
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
	if checkpointContext.getLogOutput() != "" {
		logs := checkpointContext.getLogOutput()
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

// Log appends the string representation of the provided item to the checkpoint's log output.
// If the item is nil, "nil" is appended to the log.
func (cc *CheckpointContext) Log(item interface{}) {
	if item != nil {
		cc.logOutput += fmt.Sprint(item)
	} else {
		cc.logOutput += "nil"
	}
}

// getLogOutput returns the accumulated log output for this checkpoint.
func (cc *CheckpointContext) getLogOutput() string {
	return cc.logOutput
}
