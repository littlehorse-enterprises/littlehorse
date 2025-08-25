package childthread

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const (
	ChildTaskName  string = "child-thread-task"
	ParentTaskName string = "parent-thread-task"
	WorkflowName   string = "child-thread-workflow"
)

func ParentThreadTask() string {
	return "hello there"
}

func ChildThreadTask(input string) string {
	return "Hello from child thread, the input was: " + input
}

func ChildThreadWorkflow(wf *littlehorse.WorkflowThread) {
	inputVar := wf.DeclareStr("input")
	childThread := wf.SpawnThread(
		func(child *littlehorse.WorkflowThread) {
			child.Execute(ChildTaskName, inputVar)
		},
		"my-child-thread",
		nil,
	)

	wf.Execute(ParentTaskName)
	wf.WaitForThreads(childThread)
}
