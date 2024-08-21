package internal

import (
	"github.com/spf13/cobra"
)

// executeCmd represents the run command
var scheduleCmd = &cobra.Command{
	Use:   "schedule",
	Short: "Schedule a repeated operation",
}

func init() {
	rootCmd.AddCommand(scheduleCmd)
}
