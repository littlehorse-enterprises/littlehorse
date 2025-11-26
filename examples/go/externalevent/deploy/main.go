package main

import (
	"context"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/externalevent"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	(*client).PutExternalEventDef(context.Background(),
		&lhproto.PutExternalEventDefRequest{
			Name: externalevent.NameDefName,
		},
	)

	wf := littlehorse.NewWorkflow(externalevent.ExternalEventWorkflow, externalevent.WorkflowName)
	wf.RegisterWfSpec(*client)
}
