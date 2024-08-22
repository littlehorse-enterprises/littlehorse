package childthread

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func ParentThreadTask() string {
	return "hello there"
}

func ChildThreadTask(input string) string {
	return "Hello from child thread, the input was: " + input
}

func ChildThreadWorkflow(wf *littlehorse.WorkflowThread) {
	inputVar := wf.AddVariable("input", lhproto.VariableType_STR)

	childThread := wf.SpawnThread(
		func(child *littlehorse.WorkflowThread) {
			child.Execute("child-thread-task", inputVar)
		},
		"my-child-thread",
		nil,
	)

	wf.Execute("parent-thread-task")
	wf.WaitForThreads(childThread)
}
