package main

import (
	"context"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/interrupt"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	(*client).PutExternalEventDef(context.Background(),
		&model.PutExternalEventDefRequest{
			Name: "update-tally",
		},
	)

	wf := littlehorse.NewWorkflow(interrupt.InterruptWorkflow, "interrupt-example")
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
