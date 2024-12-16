package basic

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const WorkflowName string = "basic-workflow"
const TaskDefName string = "greet"

func Greet(name *string) (string, error) {
	if *name == "" {
		return "", lh_errors.New("my-task-exception", "my-message")
	} else if *name == "obi-wan" {
		return "hello there", nil
	} else {
		return "hello, " + *name, nil
	}
}

func MyWorkflow(wf *littlehorse.WorkflowThread) {
	// nameVar := wf.AddVariableWithDefault("name", lhproto.VariableType_STR, "")
	nameVar := wf.AddVariable("name", lhproto.VariableType_STR)

	// Make it searchable
	nameVar.Searchable()

	wf.Execute(TaskDefName, nameVar)
}
