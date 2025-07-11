package main

import (
	"context"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/externalevent"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	(*client).PutExternalEventDef(context.Background(),
		&lhproto.PutExternalEventDefRequest{
			Name: externalevent.EventDefName,
		},
	)

	wf := littlehorse.NewWorkflow(externalevent.ExternalEventWorkflow, externalevent.WorkflowName)
	wf.RegisterWfSpec(*client)
}
