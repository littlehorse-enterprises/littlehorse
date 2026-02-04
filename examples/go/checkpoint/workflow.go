package checkpoint

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const WorkflowName string = "checkpoint-example"
const TaskDefName string = "greet-with-checkpoints"

// GreetWithCheckpoints demonstrates the use of ExecuteAndCheckpoint to create
// checkpoints during task execution. This allows the task to resume from the
// last checkpoint if it fails and is retried.
func GreetWithCheckpoints(name string, context *littlehorse.WorkerContext) (string, error) {
	attemptNumber := context.GetAttemptNumber()
	fmt.Printf("Hello from task worker on attempt %d before the checkpoint\n", attemptNumber)

	// First checkpoint - this expensive operation will only run once
	result, err := context.ExecuteAndCheckpoint(func(checkpointContext *littlehorse.CheckpointContext) (interface{}, error) {
		checkpointContext.Log("this is a checkpoint log")
		fmt.Printf("Hello from task worker on attempt %d in the first checkpoint\n", attemptNumber)
		return fmt.Sprintf("hello %s from first checkpoint", name), nil
	})

	if err != nil {
		return "", err
	}

	// Type assert the result
	firstResult, ok := result.(string)
	if !ok {
		return "", fmt.Errorf("failed to type assert first checkpoint result")
	}

	fmt.Println("Hello from after the first checkpoint")

	// Simulate a failure on the first attempt to demonstrate checkpoint recovery
	if attemptNumber == 0 {
		return "", fmt.Errorf("throwing a failure in the second checkpoint to show how the checkpoint works")
	}

	// Second checkpoint - this will only run on retry after the first checkpoint is restored
	result2, err := context.ExecuteAndCheckpoint(func(secondCheckpointCtx *littlehorse.CheckpointContext) (interface{}, error) {
		fmt.Println("Hi from inside the second checkpoint")
		return " and the second checkpoint", nil
	})

	if err != nil {
		return "", err
	}

	// Type assert the second result
	secondResult, ok := result2.(string)
	if !ok {
		return "", fmt.Errorf("failed to type assert second checkpoint result")
	}

	fmt.Printf("Hi from after the checkpoints on attemptNumber %d\n", attemptNumber)

	return firstResult + secondResult + " and after the second checkpoint", nil
}

func MyWorkflow(wf *littlehorse.WorkflowThread) {
	nameVar := wf.DeclareStr("name").WithDefault("World")
	nameVar.Searchable()

	wf.Execute(TaskDefName, nameVar)
}
