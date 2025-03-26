/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package internal

import (
	"errors"
	"log"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

// postEventCmd represents the postEvent command
var postEventCmd = &cobra.Command{
	Use:   "postEvent <wfRunId> <externalEventName> [(<varType> <payload>)]",
	Short: "Post an ExternalEvent to a WfRun.",
	Long: `
Post an ExternalEvent of a specified Event Type and Variable Type to a WfRun. Specifying
the Variable Type for the external event is currently required as ExternalEventDef's
currently do not carry Schema information (this will change in a future release).

The payload is deserialized according to the type. JSON objects should be provided as
a string; BYTES objects should be b64-encoded.

It's also possible to pass an empty input by excluding the last two arguments:
lhctl postEvent <wfRunId> <externalEventName>
`,
	Args: func(cmd *cobra.Command, args []string) error {
		if len(args) == 3 {
			return errors.New("requires 2 or 4 args. Note: When a <varType> is specified, a <payload> is also required")
		}
		if len(args) != 2 && len(args) != 4 {
			return errors.New("requires 2 or 4 args")
		}

		return nil
	},
	Run: func(cmd *cobra.Command, args []string) {
		wfRunIdStr, eedName := args[0], args[1]
		guid := cmd.Flag("guid").Value.String()
		threadRunNumber := int32(-1)
		nodeRunNumber := int32(-1)
		if cmd.Flag("threadRunNumber").Changed {
			threadRunFlag, err := strconv.ParseInt(cmd.Flag("threadRunNumber").Value.String(), 10, 32)
			if err != nil {
				panic("Must provide a valid threadRunNumber")
			}
			threadRunNumber = int32(threadRunFlag)
		}
		if cmd.Flag("nodeRunNumber").Changed {
			nodeRunFlag, err := strconv.ParseInt(cmd.Flag("nodeRunNumber").Value.String(), 10, 32)
			if err != nil {
				panic("Must provide a valid nodeRunNumber")
			}
			nodeRunNumber = int32(nodeRunFlag)
		}

		wfRunId := littlehorse.StrToWfRunId(wfRunIdStr)

		content := &lhproto.VariableValue{}

		if len(args) == 4 {
			varTypeStr := args[2]
			payloadStr := args[3]

			varType, validVarType := lhproto.VariableType_value[varTypeStr]
			if !validVarType {
				log.Fatal(
					"Unrecognized varType. Valid options: INT, STR, BYTES, BOOL, JSON_OBJ, JSON_ARR or DOUBLE.",
				)
			}
			varTypeEnum := lhproto.VariableType(varType)
			var err error
			content, err = littlehorse.StrToVarVal(payloadStr, varTypeEnum)
			if err != nil {
				log.Fatal("Failed deserializing payload: " + err.Error())
			}
		}

		req := lhproto.PutExternalEventRequest{
			WfRunId:            wfRunId,
			ExternalEventDefId: &lhproto.ExternalEventDefId{Name: eedName},
			Content:            content,
			Guid:               &guid,
		}
		if threadRunNumber != -1 {
			req.ThreadRunNumber = &threadRunNumber
		}
		if nodeRunNumber != -1 {
			req.NodeRunPosition = &nodeRunNumber
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).PutExternalEvent(
				requestContext(cmd),
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
