package basic

import (
	"github.com/littlehorse-eng/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-eng/littlehorse/sdk-go/wflib"
)

func Greet(name string) string {
	if name == "obi-wan" {
		return "hello there"
	} else {
		return "hello, " + name
	}
}

func MyWorkflow(thread *wflib.ThreadBuilder) {
	nameVar := thread.AddVariable("name", model.VariableTypePb_STR)
	thread.Execute("greet", nameVar)
}
