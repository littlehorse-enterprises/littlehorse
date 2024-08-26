package main

import (
	"context"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"
)

func main() {
	config, err := littlehorse.NewConfigFromProps("${HOME}/.config/littlehorse.config")
	if err != nil {
		log.Fatal(err)
	}

	client, err := config.GetGrpcClient()

	if err != nil {
		log.Fatal(err)
	}

	name := "bill"
	wfId, err := (*client).RunWf(
		context.Background(),
		&lhproto.RunWfRequest{
			WfSpecName: "my-workflow",
			Variables: map[string]*lhproto.VariableValue{
				"name": {
					Value: &lhproto.VariableValue_Str{Str: name},
				},
			},
		})
	if err != nil {
		log.Fatal(err)
	}

	log.Default().Println("got wfRunModel Id:", wfId)

}
