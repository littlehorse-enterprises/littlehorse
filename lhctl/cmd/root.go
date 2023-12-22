package cmd

import (
	"log"
	"os"

	"context"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
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

var globalClient *model.LittleHorseClient
var globalConfig *common.LHConfig

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

func getGlobalConfig(cmd *cobra.Command) common.LHConfig {
	if globalConfig != nil {
		return *globalConfig
	}

	configLoc, err := cmd.Flags().GetString("configFile")
	if err != nil {
		log.Fatal(err)
	}

	globalConfig, err = common.NewConfigFromProps(configLoc)

	if err != nil {
		globalConfig = common.NewConfigFromEnv()
	}

	return *globalConfig
}

func getGlobalClient(cmd *cobra.Command) model.LittleHorseClient {
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

func requestContext() context.Context {
	if globalConfig.TenantId != nil {
		tenantId := *globalConfig.TenantId
		md := metadata.Pairs("tenantId", tenantId)
		return metadata.NewOutgoingContext(context.Background(), md)
	}
	return context.Background()
}
