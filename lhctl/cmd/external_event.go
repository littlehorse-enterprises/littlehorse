/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"context"
	"log"
	"strings"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
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
			context.Background(),
			&model.ExternalEventIdPb{
				WfRunId:              args[0],
				ExternalEventDefName: args[1],
				Guid:                 args[2],
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

		search := &model.SearchExternalEventPb{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if wfRunId != "" {
			search.ExtEvtCriteria = &model.SearchExternalEventPb_WfRunId{
				WfRunId: wfRunId,
			}
		} else {
			var extEvtCriteria *model.SearchExternalEventPb_ExternalEventDefNameAndStatus
			if unclaimed || claimed {
				isClaimed := claimed && !unclaimed

				extEvtCriteria = &model.SearchExternalEventPb_ExternalEventDefNameAndStatus{
					ExternalEventDefNameAndStatus: &model.SearchExternalEventPb_ByExtEvtDefNameAndStatusPb{
						ExternalEventDefName: externalEventDef,
						IsClaimed:            &isClaimed,
					},
				}
			} else {
				extEvtCriteria = &model.SearchExternalEventPb_ExternalEventDefNameAndStatus{
					ExternalEventDefNameAndStatus: &model.SearchExternalEventPb_ByExtEvtDefNameAndStatusPb{
						ExternalEventDefName: externalEventDef,
					},
				}
			}
			search.ExtEvtCriteria = extEvtCriteria
		}
		common.PrintResp(getGlobalClient(cmd).SearchExternalEvent(context.Background(), search))
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

		req := &model.ListExternalEventsPb{
			WfRunId: wfRunId,
		}

		common.PrintResp(getGlobalClient(cmd).ListExternalEvents(
			context.Background(),
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
