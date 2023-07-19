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

func ExceptionHandlerWorkflow(thread *wflib.ThreadBuilder) {
	taskOutput := thread.Execute("flaky-task")

	exnToHandle := "TASK_ERROR"
	thread.HandleException(&taskOutput, &exnToHandle, func(handler *wflib.ThreadBuilder) {
		handler.Execute("some-stable-task")
	})
}
