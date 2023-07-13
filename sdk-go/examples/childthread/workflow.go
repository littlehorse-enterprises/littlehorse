package childthread

import (
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/wflib"
)

func ParentThreadTask() string {
	return "hello there"
}

func ChildThreadTask(input string) string {
	return "Hello from child thread, the input was: " + input
}

func ChildThreadWorkflow(thread *wflib.ThreadBuilder) {
	inputVar := thread.AddVariable("input", model.VariableTypePb_STR)

	childThread := thread.SpawnThread(
		func(child *wflib.ThreadBuilder) {
			child.Execute("child-thread-task", inputVar)
		},
		"my-child-thread",
		nil,
	)

	thread.Execute("parent-thread-task")
	thread.WaitForThreads(childThread)
}
