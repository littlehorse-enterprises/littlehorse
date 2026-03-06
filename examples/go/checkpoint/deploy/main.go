package main

import (
	"context"
	"log"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/checkpoint"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	wf := littlehorse.NewWorkflow(checkpoint.MyWorkflow, checkpoint.WorkflowName)
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
