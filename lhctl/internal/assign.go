package internal

import (
	"github.com/spf13/cobra"
)

// executeCmd represents the run command
var assignCmd = &cobra.Command{
	Use:   "assign",
	Short: "Assign something. Generally a UserTaskRun.",
}

func init() {
	rootCmd.AddCommand(assignCmd)
}
