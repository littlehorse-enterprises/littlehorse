package exceptionhandler

import (
	"errors"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const (
	WorkflowName   = "exception-handler"
	FlakyTaskName  = "flaky-task"
	StableTaskName = "some-stable-task"
)

func SomeStableTask() string {
	return "Hello there"
}

func FlakyTask() (*string, error) {
	return nil, errors.New("oh no! the task failed")
}

func ExceptionHandlerWorkflow(wf *littlehorse.WorkflowThread) {
	taskOutput := wf.Execute(FlakyTaskName)
	exnToHandle := littlehorse.TaskError
	wf.HandleError(taskOutput, &exnToHandle, func(handler *littlehorse.WorkflowThread) {
		handler.Execute(StableTaskName)
	})
}
