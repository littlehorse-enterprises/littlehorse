package internal

import (
	"github.com/spf13/cobra"
)

// executeCmd represents the run command
var putCmd = &cobra.Command{
	Use:   "put",
	Short: "Create or update an object",
}

func init() {
	rootCmd.AddCommand(putCmd)
}
