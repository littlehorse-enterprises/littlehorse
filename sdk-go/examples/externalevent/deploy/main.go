package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/externalevent"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	client.PutExternalEventDef(&model.PutExternalEventDefRequest{
		Name: "my-name",
	}, true)

	wf := wflib.NewWorkflow(externalevent.ExternalEventWorkflow, "external-event")
	putWf, err := wf.Compile()
	if err != nil {
		log.Fatal(err)
	}

	resp, err := client.PutWfSpec(putWf)
	if err != nil {
		log.Fatal(err)
	}
	common.PrintProto(resp)
}
