/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"github.com/spf13/cobra"
)

// resumeCmd represents the resume command
var resumeCmd = &cobra.Command{
	Use:   "resume",
	Short: "Resume a resource.",
	Long: `Resume a resource. Supported resources:
- wfRun
`,
}

func init() {
	rootCmd.AddCommand(resumeCmd)
}
