/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package internal

import (
	"github.com/spf13/cobra"
)

// stopCmd represents the stop command
var stopCmd = &cobra.Command{
	Use:   "stop",
	Short: "Stop a resource.",
	Long: `Stop a resource. Supported resources:
- wfRun
`,
}

func init() {
	rootCmd.AddCommand(stopCmd)
}
