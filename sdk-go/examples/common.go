package examples

import (
	"log"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common"
)

func LoadConfigAndClient() (*common.LHConfig, *common.LHClient) {
	config, err := common.NewConfigFromProps("${HOME}/.config/littlehorse.config")
	if err != nil {
		log.Fatal(err)
	}
	client, err := common.NewLHClient(config)
	if err != nil {
		log.Fatal(err)
	}
	return config, client
}
