package internal

import (
	"github.com/spf13/cobra"
)

var countCmd = &cobra.Command{
	Use:   "count",
	Short: "Count API Resources based on criteria.",
	Long: `Returns the count of API Resources matching the given criteria.

Each resource (eg. NodeRun) has different criteria options.
For information about how to count a specific resource, consult:
'lhctl count <resourceType> --help'
`,
}

func init() {
	rootCmd.AddCommand(countCmd)
}
