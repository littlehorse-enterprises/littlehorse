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
	nameVar := thread.AddVariableWithDefault("name", model.VariableType_STR, "Qui-Gon Jinn")
	thread.Execute("greet", nameVar)
}
