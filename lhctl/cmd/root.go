package cmd

import (
	"log"
	"os"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

// rootCmd represents the base command when called without any subcommands
var rootCmd = &cobra.Command{
	Use:   "lhctl",
	Short: "A brief description of your application",
	Long: `A longer description that spans multiple lines and likely contains
examples and usage of using your application. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	// Uncomment the following line if your bare application
	// has an action associated with it:
	// Run: func(cmd *cobra.Command, args []string) { },
}

var globalClient *model.LHPublicApiClient
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

func getGlobalClient(cmd *cobra.Command) model.LHPublicApiClient {
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
