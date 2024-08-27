package internal

import (
	"github.com/spf13/cobra"
)

// searchCmd represents the search command
var searchCmd = &cobra.Command{
	Use:   "search",
	Short: "Search for API Resources based on criteria.",
	Long: `Retrieves a list of API Resources based on search criteria.

Each resource (eg. WfRun, TaskDef, NodeRun, etc) has different search criteria.
For information about how to search for a specific resource, consult:
'lhctl search <resourceType> --help'
`,
}

func init() {
	rootCmd.AddCommand(searchCmd)
	searchCmd.PersistentFlags().Int32("limit", 100, "Guideline for number of response items to fetch per request.")
	searchCmd.PersistentFlags().BytesBase64("bookmark", nil, "Optional bookmark for paginated scans.")
}
