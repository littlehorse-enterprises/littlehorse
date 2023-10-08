package childthread

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func ParentThreadTask() string {
	return "hello there"
}

func ChildThreadTask(input string) string {
	return "Hello from child thread, the input was: " + input
}

func ChildThreadWorkflow(wf *wflib.WorkflowThread) {
	inputVar := wf.AddVariable("input", model.VariableType_STR)

	childThread := wf.SpawnThread(
		func(child *wflib.WorkflowThread) {
			child.Execute("child-thread-task", inputVar)
		},
		"my-child-thread",
		nil,
	)

	wf.Execute("parent-thread-task")
	wf.WaitForThreads(childThread)
}
