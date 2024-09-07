package taskmetadata

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"strconv"
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
	WorkflowName = "task-metadata-workflow"
)

func GetInfo(input *InputData, context *littlehorse.WorkerContext) string {
	context.Log("running the GetInfo()")
	return "the id for " + input.Art.Title + " is: " + strconv.Itoa(input.Art.Id) + " and WfRunId: " + context.GetWfRunId().GetId()
}

func MyWorkflowGet(wf *littlehorse.WorkflowThread) {
	inputVar := wf.AddVariable("input", lhproto.VariableType_JSON_OBJ)
	wf.Execute(TaskDefName, inputVar)
}
