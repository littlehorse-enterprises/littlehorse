package basic

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func Greet(name string) string {
	if name == "obi-wan" {
		return "hello there"
	} else {
		return "hello, " + name
	}
}

func MyWorkflow(wf *wflib.WorkflowThread) {
	nameVar := wf.AddVariableWithDefault("name", model.VariableType_STR, "Qui-Gon Jinn")

	// Make it searchable
	nameVar.Searchable()

	wf.Execute("greet", nameVar)
}
