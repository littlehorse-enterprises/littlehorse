package jsonarray

import (
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

const (
	TaskDefName  = "greet"
	WorkflowName = "json-array-workflow"
)

func AddUpList(inputs *[]InputData) int {
	result := 0
	for _, inputData := range *inputs {
		result += inputData.Foo
	}
	return result
}

func MyWorkflowAdd(wf *littlehorse.WorkflowThread) {
	inputVar := wf.DeclareJsonArr("input")
	wf.Execute(TaskDefName, inputVar)
}
