package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/conditionals/while"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func main() {
	_, client := examples.LoadConfigAndClient()
	wf := wflib.NewWorkflow(while.DonutWorkflow, "donut-workflow")
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
