package internal

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

var putWorkflowEventDefCmd = &cobra.Command{
	Use:   "workflowEventDef <name> <type>",
	Short: "Create a WorkflowEventDef.",
	Long: `Create a WorkflowEventDef.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		name, _ := cmd.Flags().GetString("name")
		contentTypeStr, _ := cmd.Flags().GetString("type")

		contentType, validContentType := lhproto.VariableType_value[contentTypeStr]
		if !validContentType {
			log.Fatal(
				"Unrecognized varType. Valid options: INT, STR, BYTES, BOOL, JSON_OBJ, JSON_ARR or DOUBLE.",
			)
		}
		contentTypeEnum := lhproto.VariableType(contentType)

		pwed := lhproto.PutWorkflowEventDefRequest{
			Name: name,
			Type: contentTypeEnum,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).PutWorkflowEventDef(requestContext(cmd), &pwed))
	},
}

// getWorkflowEventDefCmd represents the getWorkflowEventDef command
var getWorkflowEventDefCmd = &cobra.Command{
	Use:   "workflowEventDef <name>",
	Short: "Get an WorkflowEventDef by name.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the Name of WorkflowEventDef to get.")
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).GetWorkflowEventDef(
				requestContext(cmd),
				&lhproto.WorkflowEventDefId{
					Name: args[0],
				},
			),
		)
	},
}

var searchWorkflowEventDefCmd = &cobra.Command{
	Use:   "workflowEventDef",
	Short: "Search for WorkflowEventDef",
	Long: `Search for WorkflowEventDefs.

No option groups for Search WorkflowEventDef are supported. Therefore, this command
searches for all WorkflowEventDefs.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		prefix, _ := cmd.Flags().GetString("prefix")

		littlehorse.PrintResp(
			getGlobalClient(cmd).SearchWorkflowEventDef(
				requestContext(cmd),
				&lhproto.SearchWorkflowEventDefRequest{
					Bookmark: bookmark,
					Limit:    &limit,
					Prefix:   &prefix,
				}),
		)
	},
}

var deleteWorkflowEventDefCmd = &cobra.Command{
	Use:   "workflowEventDef <name> <version>",
	Short: "Delete a WorkflowEventDef.",
	Long: `Delete a WorkflowEventDef. You must provide the name of the
WorkflowEventDef to delete.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: Name of WorkflowEventDef to Delete")

		}

		name := args[0]

		littlehorse.PrintResp(
			getGlobalClient(cmd).DeleteWorkflowEventDef(
				requestContext(cmd),
				&lhproto.DeleteWorkflowEventDefRequest{
					Id: &lhproto.WorkflowEventDefId{
						Name: name,
					},
				}),
		)
	},
}

func init() {
	getCmd.AddCommand(getWorkflowEventDefCmd)

	putCmd.AddCommand(putWorkflowEventDefCmd)
	putWorkflowEventDefCmd.Flags().String("name", "", "The name of the WorkflowEventDef")
	putWorkflowEventDefCmd.MarkFlagRequired("name")

	putWorkflowEventDefCmd.Flags().String("type", "", "The type of 'content' thrown with a WorkflowEvent based on this WorkflowEventDef.")
	putWorkflowEventDefCmd.MarkFlagRequired("type")

	searchCmd.AddCommand(searchWorkflowEventDefCmd)
	searchWorkflowEventDefCmd.Flags().String("prefix", "", "Prefix of name of WorkflowEventDefs to search for.")

	deleteCmd.AddCommand(deleteWorkflowEventDefCmd)
}
