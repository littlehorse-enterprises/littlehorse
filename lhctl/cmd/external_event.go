/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"log"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
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

		if len(args) != 1 && len(args) != 3 {
			needsHelp = true
		}

		if needsHelp {
			log.Fatal("Must provide 1 or 3 arguments. See 'lhctl get externalEvent -h'")
		}

		common.PrintResp(getGlobalClient(cmd).GetExternalEvent(
			requestContext(),
			&model.ExternalEventId{
				WfRunId:            &model.WfRunId{Id: args[0]},
				ExternalEventDefId: &model.ExternalEventDefId{Name: args[1]},
				Guid:               args[2],
			},
		))
	},
}

var searchExternalEventCommand = &cobra.Command{
	Use:   "externalEvent",
	Short: "Search for ExternalEvent's by WfRunId",
	Long: `
Search for ExternalEvent's by the WfRunId.

Returns a list of ObjectId's that can be passed into 'lhctl get externalEvent'.

Choose one of the following option groups:
[wfRunId]
[externalEventDef]
[externalEventDef, claimed]
[externalEventDef, unclaimed]
`,
	Run: func(cmd *cobra.Command, args []string) {

		wfRunId, _ := cmd.Flags().GetString("wfRunId")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		externalEventDef, _ := cmd.Flags().GetString("externalEventDef")
		claimed, _ := cmd.Flags().GetBool("claimed")
		unclaimed, _ := cmd.Flags().GetBool("unclaimed")

		search := &model.SearchExternalEventRequest{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if wfRunId != "" {
			search.ExtEvtCriteria = &model.SearchExternalEventRequest_WfRunId{
				WfRunId: &model.WfRunId{Id: wfRunId},
			}
		} else {
			var extEvtCriteria *model.SearchExternalEventRequest_ExternalEventDefNameAndStatus
			if unclaimed || claimed {
				isClaimed := claimed && !unclaimed

				extEvtCriteria = &model.SearchExternalEventRequest_ExternalEventDefNameAndStatus{
					ExternalEventDefNameAndStatus: &model.SearchExternalEventRequest_ByExtEvtDefNameAndStatusRequest{
						ExternalEventDefName: externalEventDef,
						IsClaimed:            &isClaimed,
					},
				}
			} else {
				extEvtCriteria = &model.SearchExternalEventRequest_ExternalEventDefNameAndStatus{
					ExternalEventDefNameAndStatus: &model.SearchExternalEventRequest_ByExtEvtDefNameAndStatusRequest{
						ExternalEventDefName: externalEventDef,
					},
				}
			}
			search.ExtEvtCriteria = extEvtCriteria
		}
		common.PrintResp(getGlobalClient(cmd).SearchExternalEvent(requestContext(), search))
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

		req := &model.ListExternalEventsRequest{
			WfRunId: &model.WfRunId{Id: wfRunId},
		}

		common.PrintResp(getGlobalClient(cmd).ListExternalEvents(
			requestContext(),
			req,
		))
	},
}

func init() {
	getCmd.AddCommand(getExternalEventCmd)
	searchCmd.AddCommand(searchExternalEventCommand)
	listCmd.AddCommand(listExternalEventCmd)

	searchExternalEventCommand.Flags().String("wfRunId", "", "WfRunId of ExternalEvent's to search for")
	searchExternalEventCommand.Flags().String("externalEventDef", "", "ExternalEventDef name of ExternalEvent's to search for")
	searchExternalEventCommand.Flags().Bool("claimed", false, "List only claimed events")
	searchExternalEventCommand.Flags().Bool("unclaimed", false, "List only unclaimed events")
	searchExternalEventCommand.MarkFlagsMutuallyExclusive("claimed", "unclaimed")
}
