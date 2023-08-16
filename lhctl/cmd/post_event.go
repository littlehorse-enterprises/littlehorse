/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"context"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

// postEventCmd represents the postEvent command
var postEventCmd = &cobra.Command{
	Use:   "postEvent <wfRunId> <externalEventName> <varType> <payload>",
	Short: "Post an ExternalEvent to a WfRun.",
	Long: `
Post an ExternalEvent of a specified Event Type and Variable Type to a WfRun. Specifying
the Variable Type for the external event is currently required as ExternalEventDef's
currently do not carry Schema information (this will change in a future release).

The payload is deserialized according to the type. JSON objects should be provided as
a string; BYTES objects should be b64-encoded.

It's also possible to pass a null input:
lhctl postEvent <wfRunId> <externalEventName> NULL
`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) < 3 {
			log.Fatal("Required args: <wfRunId> <externalEventName> <varType> <payload> or  <wfRunId> <externalEventName> NULL")
		}

		wfRunId, eedName, varTypeStr := args[0], args[1], args[2]
		varType, validVarType := model.VariableType_value[varTypeStr]

		if !validVarType {
			log.Fatal(
				"Unrecognized varType. Valid options: INT, STR, BYTES, BOOL, JSON_OBJ, JSON_ARR, DOUBLE or NULL.",
			)
		}

		varTypeEnum := model.VariableType(varType)

		content := &model.VariableValue{
			Type: model.VariableType_NULL,
		}

		if len(args) == 3 && varTypeEnum != model.VariableType_NULL {
			log.Fatal(
				"Payload is required",
			)
		}

		if len(args) == 4 {
			payloadStr := args[3]

			var err error
			content, err = common.StrToVarVal(payloadStr, varTypeEnum)
			if err != nil {
				log.Fatal("Failed deserializing payload: " + err.Error())
			}
		}

		req := model.PutExternalEventPb{
			WfRunId:              wfRunId,
			ExternalEventDefName: eedName,
			Content:              content,
		}

		common.PrintResp(
			getGlobalClient(cmd).PutExternalEvent(
				context.Background(),
				&req,
			),
		)
	},
}

func init() {
	rootCmd.AddCommand(postEventCmd)
	postEventCmd.Flags().Int32("threadRunNumber", -1, "ThreadRunNumber to send event to.")
	postEventCmd.Flags().Int32("nodeRunNumber", -1, "NodeRunPosition to send event to.")
	postEventCmd.Flags().String("guid", "", "Optional guid for event (for idempotence).")
}
