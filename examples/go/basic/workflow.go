package basic

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const WorkflowName string = "basic-workflow"
const TaskDefName string = "greet"

func Greet(name string) string {
	if name == "obi-wan" {
		return "hello there"
	} else {
		return "hello, " + name
	}
}

func MyWorkflow(wf *littlehorse.WorkflowThread) {
	nameVar := wf.DeclareStr("name").WithDefault("Qui-Gon Jinn")

	// Make it searchable
	nameVar.Searchable()

	wf.Execute(TaskDefName, nameVar)
}
