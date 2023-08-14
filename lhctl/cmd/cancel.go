package cmd

import (
	"github.com/spf13/cobra"
)

// executeCmd represents the run command
var cancelUserTaskCmd = &cobra.Command{
	Use:   "cancel",
	Short: "Cancel",
}

func init() {
	rootCmd.AddCommand(cancelUserTaskCmd)
}
