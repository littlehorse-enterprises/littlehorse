package cmd

import (
	"github.com/spf13/cobra"
)

// executeCmd represents the run command
var createCmd = &cobra.Command{
	Use:   "create",
	Short: "Create an object",
}

func init() {
	rootCmd.AddCommand(createCmd)
}
