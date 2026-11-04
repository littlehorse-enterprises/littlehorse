package internal

import (
	"github.com/spf13/cobra"
)

// executeCmd groups commands that execute tasks.
var executeCmd = &cobra.Command{
	Use:   "execute",
	Short: "Execute a Task or a UserTaskRun.",
}

func init() {
	rootCmd.AddCommand(executeCmd)
}
