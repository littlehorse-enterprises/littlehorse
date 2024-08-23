package examples

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"
	"os"
)

func LoadConfigAndClient() (*littlehorse.LHConfig, *lhproto.LittleHorseClient) {
	home, err := os.UserHomeDir()
	if err != nil {
		log.Fatal(err)
	}

	configPath := home + "/.config/littlehorse.config"
	config := littlehorse.NewConfigFromEnv()

	if _, err := os.Stat(configPath); err == nil {
		config, err = littlehorse.NewConfigFromProps(configPath)
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
