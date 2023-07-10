package basic

import (
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/wflib"
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
