package examples

import (
	"log"
	"os"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func LoadConfigAndClient() (*common.LHConfig, *model.LittleHorseClient) {
	home, err := os.UserHomeDir()
	if err != nil {
		log.Fatal(err)
	}

	configPath := home + "/.config/littlehorse.config"
	config := common.NewConfigFromEnv()

	if _, err := os.Stat(configPath); err == nil {
		config, err = common.NewConfigFromProps(configPath)
		if err != nil {
			log.Fatal(err)
		}
	}

	client, err := config.GetGrpcClient()
	if err != nil {
		log.Fatal(err)
	}

	return config, client
}
