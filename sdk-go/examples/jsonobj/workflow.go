package jsonobj

import (
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
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

func MyWorkflowGet(wf *wflib.WorkflowThread) {
	inputVar := wf.AddVariable("input", model.VariableType_JSON_OBJ)
	wf.Execute("greet", inputVar)
}
