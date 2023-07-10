package main

import (
	"log"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/examples"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/examples/externalevent"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/wflib"
)

func main() {
	_, client := examples.LoadConfigAndClient()

	client.PutExternalEventDef(&model.PutExternalEventDefPb{
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
