/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package internal

import (
	"fmt"
	"log"
	"strings"

	"github.com/spf13/cobra"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

// getExternalEventCmd represents the externalEvent command
var getExternalEventCmd = &cobra.Command{
	Use:   "externalEvent <wfRunId> <externalEventDefName> <guid>",
	Short: "Get an ExternalEvent by identifiers.",
	Long: `ExternalEvent's are identified uniquely by the combination of the following:
	- Associated WfRun Id
	- ExternalEventDef name
	- A unique guid

	You may provide all three identifiers as three separate arguments or you may provide
	them delimited by the '/' character, as returned in all 'search' command queries.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 3 {
			needsHelp = true
		}

		if needsHelp {
			log.Fatal("Must provide 1 or 3 arguments. See 'lhctl get externalEvent -h'")
		}

		fmt.Println(args)

		ctx := requestContext(cmd)

		littlehorse.PrintResp(getGlobalClient(cmd).GetExternalEvent(
			ctx,
			&lhproto.ExternalEventId{
				WfRunId:            littlehorse.StrToWfRunId(args[0]),
				ExternalEventDefId: &lhproto.ExternalEventDefId{Name: args[1]},
				Guid:               args[2],
			},
		))
	},
}

var searchExternalEventCmd = &cobra.Command{
	Use:   "externalEvent",
	Short: "Search for ExternalEvent's by ExternalEventDef Name",
	Long: `
Search for ExternalEvent's by their ExternalEventDef Name.

Returns a list of ObjectId's that can be passed into 'lhctl get externalEvent'.

* Note: '--isClaimed' is a Boolean flag with 3 states:
	- return ALL (flag is not present)
	- return only TRUE (flag is present or reads '--isClaimed=true')
	- return only FALSE (flag reads '--isClaimed=false')

* Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
	options with this group to put a time bound on ExternalEvents which are
	returned. The time bound applies to the time that the ExternalEvents
	were created.
`,
	Run: func(cmd *cobra.Command, args []string) {

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		externalEventDefName, _ := cmd.Flags().GetString("externalEventDefName")
		isClaimed, _ := cmd.Flags().GetBool("isClaimed")

		earliest, latest := loadEarliestAndLatestStart(cmd)

		search := &lhproto.SearchExternalEventRequest{
			Bookmark:      bookmark,
			Limit:         &limit,
			EarliestStart: earliest,
			LatestStart:   latest,
			ExternalEventDefId: &lhproto.ExternalEventDefId{
				Name: externalEventDefName,
			},
		}

		if cmd.Flags().Lookup("isClaimed").Changed {
			search.IsClaimed = &isClaimed
		}

		littlehorse.PrintResp(getGlobalClient(cmd).SearchExternalEvent(requestContext(cmd), search))
	},
}

var listExternalEventCmd = &cobra.Command{
	Use:   "externalEvent <wfRunId>",
	Short: "List all ExternalEvent's for a given WfRun Id.",
	Long: `
Lists all ExternalEvent's for a given WfRun Id.
`,
	Run: func(cmd *cobra.Command, args []string) {
		// bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		// limit, _ := cmd.Flags().GetInt32("limit")

		if len(args) != 1 {
			log.Fatal("Must provide one arg: the WfRun ID!")
		}
		wfRunId := args[0]

		req := &lhproto.ListExternalEventsRequest{
			WfRunId: littlehorse.StrToWfRunId(wfRunId),
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ListExternalEvents(
			requestContext(cmd),
			req,
		))
	},
}

func init() {
	getCmd.AddCommand(getExternalEventCmd)
	searchCmd.AddCommand(searchExternalEventCmd)
	listCmd.AddCommand(listExternalEventCmd)

	searchExternalEventCmd.Flags().String("externalEventDefName", "", "ExternalEventDef Name of ExternalEvents to search for")
	searchExternalEventCmd.Flags().Bool("isClaimed", false, "List only ExternalEvents that are claimed")
	searchExternalEventCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for Principals that were created no more than this number of minutes ago")
	searchExternalEventCmd.Flags().Int("latestMinutesAgo", -1, "Search only for Principals that were created at least this number of minutes ago")
}
