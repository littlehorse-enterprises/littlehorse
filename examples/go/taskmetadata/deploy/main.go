package main

import (
	"context"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/taskmetadata"
)

func main() {
	_, client := examples.LoadConfigAndClient()
	wf := littlehorse.NewWorkflow(taskmetadata.MyWorkflowGet, taskmetadata.WorkflowName)
	putWf, err := wf.Compile()
	if err != nil {
		log.Fatal(err)
	}

	resp, err := (*client).PutWfSpec(context.Background(), putWf)
	if err != nil {
		log.Fatal(err)
	}
	littlehorse.PrintProto(resp)
}
