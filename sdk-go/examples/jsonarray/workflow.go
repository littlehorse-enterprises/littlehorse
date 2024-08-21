package jsonarray

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
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

func AddUpList(inputs *[]InputData) int {
	result := 0
	for _, inputData := range *inputs {
		result += inputData.Foo
	}
	return result
}

func MyWorkflowAdd(wf *littlehorse.WorkflowThread) {
	inputVar := wf.AddVariable("input", model.VariableType_JSON_ARR)
	wf.Execute("greet", inputVar)
}
