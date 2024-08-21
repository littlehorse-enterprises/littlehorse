package main

import (
	"context"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/externalevent"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	(*client).PutExternalEventDef(context.Background(),
		&model.PutExternalEventDefRequest{
			Name: "my-name",
		},
	)

	wf := littlehorse.NewWorkflow(externalevent.ExternalEventWorkflow, "external-event")
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
