package runchildworkflow

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const (
	// Match Java example task & workflow names
	TaskName           string = "greet"
	ChildWorkflowName  string = "some-other-wfspec"
	ParentWorkflowName string = "my-parent"
)

func Greet(name string) string {
	return "hello there, " + name
}

func ChildWorkflow(wf *littlehorse.WorkflowThread) {
	childInput := wf.DeclareStr("child-input-name").WithDefault("little-child")
	wf.Execute(TaskName, childInput)
}

func ParentWorkflow(wf *littlehorse.WorkflowThread) {
	nameVar := wf.DeclareStr("input-name").WithDefault("parent-name")
	childOutput := wf.DeclareStr("child-output")

	// Run the child workflow and pass the declared variable as input
	child := wf.RunWf(ChildWorkflowName, map[string]interface{}{"child-input-name": nameVar})

	// Do some parent work
	wf.Execute(TaskName, "hi from parent")

	// Wait for the child workflow to finish and assign its result
	childOutput.Assign(wf.WaitForChildWf(child))
}
