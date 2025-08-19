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
	Args: cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		name := args[0]
		contentTypeStr := args[1]

		contentType, validContentType := lhproto.VariableType_value[contentTypeStr]
		if !validContentType {
			log.Fatalf(
				"Unrecognized type: '%s'. Valid options: INT, STR, BYTES, BOOL, JSON_OBJ, JSON_ARR or DOUBLE.",
				args[1],
			)
		}
		contentTypeEnum := lhproto.VariableType(contentType)

		returnType := lhproto.ReturnType{
			ReturnType: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_PrimitiveType{
					PrimitiveType: contentTypeEnum,
				},
				Masked: false,
			},
		}

		pwed := lhproto.PutWorkflowEventDefRequest{
			Name:        name,
			ContentType: &returnType,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).PutWorkflowEventDef(requestContext(cmd), &pwed))
	},
}

// getWorkflowEventDefCmd represents the getWorkflowEventDef command
var getWorkflowEventDefCmd = &cobra.Command{
	Use:   "workflowEventDef <name>",
	Short: "Get an WorkflowEventDef by name.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {

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
	Args: cobra.ExactArgs(0),
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
	Use:   "workflowEventDef <name>",
	Short: "Delete a WorkflowEventDef.",
	Long: `Delete a WorkflowEventDef. You must provide the name of the
WorkflowEventDef to delete.
	`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {

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

	searchCmd.AddCommand(searchWorkflowEventDefCmd)
	searchWorkflowEventDefCmd.Flags().String("prefix", "", "Prefix of name of WorkflowEventDefs to search for.")

	deleteCmd.AddCommand(deleteWorkflowEventDefCmd)
}
