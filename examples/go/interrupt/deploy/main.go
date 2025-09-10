package main

import (
	"context"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/interrupt"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	(*client).PutExternalEventDef(context.Background(),
		&lhproto.PutExternalEventDefRequest{
			Name: interrupt.UpdateTallyInterruptName,
		},
	)

	wf := littlehorse.NewWorkflow(interrupt.InterruptWorkflow, interrupt.WorkflowName)
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
