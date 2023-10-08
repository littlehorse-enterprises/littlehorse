package exceptionhandler

import (
	"errors"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func SomeStableTask() string {
	return "Hello there"
}

func FlakyTask() (*string, error) {
	return nil, errors.New("oh no! the task failed")
}

func ExceptionHandlerWorkflow(wf *wflib.WorkflowThread) {
	taskOutput := wf.Execute("flaky-task")

	exnToHandle := "TASK_ERROR"
	wf.HandleException(&taskOutput, &exnToHandle, func(handler *wflib.WorkflowThread) {
		handler.Execute("some-stable-task")
	})
}
