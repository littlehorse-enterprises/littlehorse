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
	nameVar := wf.AddVariable("name", model.VariableType_STR).Required()

	// Make it searchable
	nameVar.Searchable()

	wf.Execute("greet", nameVar)
}
