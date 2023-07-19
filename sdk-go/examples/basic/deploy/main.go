package main

import (
	"log"

	"github.com/littlehorse-eng/littlehorse/sdk-go/common"
	"github.com/littlehorse-eng/littlehorse/sdk-go/examples"
	"github.com/littlehorse-eng/littlehorse/sdk-go/examples/basic"
	"github.com/littlehorse-eng/littlehorse/sdk-go/wflib"
)

func main() {
	_, client := examples.LoadConfigAndClient()
	wf := wflib.NewWorkflow(basic.MyWorkflow, "my-workflow")
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
