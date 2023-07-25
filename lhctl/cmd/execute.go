package cmd

import (
	"github.com/spf13/cobra"
)

// executeCmd represents the run command
var executeCmd = &cobra.Command{
	Use:   "execute",
	Short: "Execute something. Generally a UserTaskRun.",
}

func init() {
	rootCmd.AddCommand(executeCmd)
}
