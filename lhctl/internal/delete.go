/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package internal

import (
	"github.com/spf13/cobra"
)

// deleteCmd represents the delete command
var deleteCmd = &cobra.Command{
	Use:   "delete",
	Short: "Delete a resource.",
	Long: `Delete a resource. Supported resources:
- wfRun
`,
}

func init() {
	rootCmd.AddCommand(deleteCmd)
}
