package jsonobj

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

type InputData struct {
	Foo int     `json:"foo"`
	Art Article `json:"art"`
}

type Article struct {
	Id      int    `json:"id"`
	Title   string `json:"title,omitempty"`
	Content string `json:"content,omitempty"`
}

func GetInfo(input *InputData) string {
	return "the id for " + input.Art.Title + " is: " + strconv.Itoa(input.Art.Id)
}

func MyWorkflowGet(wf *littlehorse.WorkflowThread) {
	inputVar := wf.AddVariable("input", model.VariableType_JSON_OBJ)
	wf.Execute("greet", inputVar)
}
