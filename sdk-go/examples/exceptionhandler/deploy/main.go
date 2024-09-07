package main

import (
	"context"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/exceptionhandler"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	wf := littlehorse.NewWorkflow(exceptionhandler.ExceptionHandlerWorkflow, exceptionhandler.WorkflowName)
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
