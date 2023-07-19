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

func MyWorkflowGet(thread *wflib.ThreadBuilder) {
	inputVar := thread.AddVariable("input", model.VariableTypePb_JSON_OBJ)
	thread.Execute("greet", inputVar)
}
