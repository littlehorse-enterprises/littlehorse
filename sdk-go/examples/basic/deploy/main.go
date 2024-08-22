package main

import (
	"context"
	lh "github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/basic"
)

func main() {
	_, client := examples.LoadConfigAndClient()
	wf := lh.NewWorkflow(basic.MyWorkflow, basic.WorkflowName)
	putWf, err := wf.Compile()
	if err != nil {
		log.Fatal(err)
	}

	resp, err := (*client).PutWfSpec(context.Background(), putWf)
	if err != nil {
		log.Fatal(err)
	}
	lh.PrintProto(resp)
}
