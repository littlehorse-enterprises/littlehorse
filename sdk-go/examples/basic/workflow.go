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

func MyWorkflow(thread *wflib.ThreadBuilder) {
	nameVar := thread.AddVariable("name", model.VariableType_STR)
	thread.Execute("greet", nameVar)
}
