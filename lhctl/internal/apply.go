package internal

import (
	"github.com/spf13/cobra"
)

// applyCmd represents the apply command
var applyCmd = &cobra.Command{
	Use:   "apply",
	Short: "Apply a resource to a running object.",
}

func init() {
	rootCmd.AddCommand(applyCmd)
}
