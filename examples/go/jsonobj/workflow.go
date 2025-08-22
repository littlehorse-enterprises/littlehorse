package jsonobj

import (
	"strconv"

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
	WorkflowName = "json-object-workflow"
)

func GetInfo(input *InputData) string {
	return "the id for " + input.Art.Title + " is: " + strconv.Itoa(input.Art.Id)
}

func MyWorkflowGet(wf *littlehorse.WorkflowThread) {
	inputVar := wf.DeclareJsonObj("input")
	wf.Execute(TaskDefName, inputVar)
}
