package internal

import (
	"github.com/spf13/cobra"
)

var editCmd = &cobra.Command{
	Use:   "edit",
	Short: "Edit an object",
}

func init() {
	rootCmd.AddCommand(editCmd)
}
