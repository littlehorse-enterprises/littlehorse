package main

import (
	"context"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/conditionals/ifelse"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func main() {
	_, client := examples.LoadConfigAndClient()
	wf := wflib.NewWorkflow(ifelse.DonutWorkflow, "donut-workflow")
	putWf, err := wf.Compile()
	if err != nil {
		log.Fatal(err)
	}

	resp, err := (*client).PutWfSpec(context.Background(), putWf)
	if err != nil {
		log.Fatal(err)
	}
	common.PrintProto(resp)
}
