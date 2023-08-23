package examples

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func LoadConfigAndClient() (*common.LHConfig, *model.LHPublicApiClient) {
	config, err := common.NewConfigFromProps("${HOME}/.config/littlehorse.config")
	if err != nil {
		log.Fatal(err)
	}
	client, err := config.GetGrpcClient()
	if err != nil {
		log.Fatal(err)
	}
	return config, client
}
