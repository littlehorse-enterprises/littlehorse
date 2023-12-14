package main

import (
	"context"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func main() {
	config, err := common.NewConfigFromProps("${HOME}/.config/littlehorse.config")
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
		&model.RunWfRequest{
			WfSpecName: "my-workflow",
			Variables: map[string]*model.VariableValue{
				"name": {
					Value: &model.VariableValue_Str{Str: name},
				},
			},
		})
	if err != nil {
		log.Fatal(err)
	}

	log.Default().Println("got wfRunModel Id:", wfId)

}
