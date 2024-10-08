package internal

import (
	"log"
	"os"

	"context"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/grpc/metadata"
)

// rootCmd represents the base command when called without any subcommands
var rootCmd = &cobra.Command{
	Use:   "lhctl",
	Short: "Interact with the LittleHorse API",
	Long: `LittleHorse CLI: lhctl allows you to perform almost any action against a LittleHorse
cluster, ranging from managing metadata (WfSpec, TaskDef, etc) to running
a WfRun, to searching for various objects.
`,
}

var globalClient *lhproto.LittleHorseClient
var globalConfig *littlehorse.LHConfig

// Execute adds all child commands to the root command and sets flags appropriately.
// This is called by main.main(). It only needs to happen once to the rootCmd.
func Execute() {
	err := rootCmd.Execute()
	if err != nil {
		os.Exit(1)
	}
}

func init() {
	rootCmd.PersistentFlags().String(
		"configFile",
		"${HOME}/.config/littlehorse.config",
		"Configuration File Location",
	)
}

func getGlobalConfig(cmd *cobra.Command) littlehorse.LHConfig {
	if globalConfig != nil {
		return *globalConfig
	}

	configLoc, err := cmd.Flags().GetString("configFile")
	if err != nil {
		log.Fatal(err)
	}

	globalConfig, err = littlehorse.NewConfigFromProps(configLoc)

	if err != nil {
		if os.IsNotExist(err) {
			if configLoc != "${HOME}/.config/littlehorse.config" {
				log.Fatal("provided config file does not exist")
			}
		} else {
			log.Fatal(err)
		}
		globalConfig = littlehorse.NewConfigFromEnv()
	}

	return *globalConfig
}

func getGlobalClient(cmd *cobra.Command) lhproto.LittleHorseClient {
	if globalClient != nil {
		return *globalClient
	}

	config := getGlobalConfig(cmd)

	var err error

	globalClient, err = config.GetGrpcClient()
	if err != nil {
		log.Fatal(err)
	}

	return *globalClient
}

func requestContext(cmd *cobra.Command) context.Context {
	if getGlobalConfig(cmd).TenantId != nil {
		tenantId := *globalConfig.TenantId
		md := metadata.Pairs("tenantId", tenantId)
		return metadata.NewOutgoingContext(context.Background(), md)
	}
	return context.Background()
}
