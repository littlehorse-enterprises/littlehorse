package main

import (
	"log"

	"github.com/littlehorse-eng/littlehorse/sdk-go/common"
)

func main() {
	config, err := common.NewConfigFromProps("${HOME}/.config/littlehorse.config")
	if err != nil {
		log.Fatal(err)
	}

	client, err := common.NewLHClient(config)

	if err != nil {
		log.Fatal(err)
	}

	wfId, err := client.RunWf("my-workflow", nil, nil, common.WfArg{Name: "name", Arg: "bill"})
	if err != nil {
		log.Fatal(err)
	}

	log.Default().Println("got wfRun Id:", *wfId)

}
