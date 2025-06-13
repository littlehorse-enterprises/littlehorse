package internal

import (
	"errors"
	"log"
	"strings"

	"github.com/spf13/cobra"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

// getExternalEventCmd represents the externalEvent command
var getCorrelatedEventCmd = &cobra.Command{
	Use:   "correlatedEvent <key> <externalEventDefName>",
	Short: "Get a CorrelatedEvent by identifiers.",
	Long: `CorrelatedEvent's are identified uniquely by the combination of the following:
	- Key (Correlation ID)
	- ExternalEventDef name

	You may provide both identifiers as two separate arguments or you may provide
	them delimited by the '/' character, as returned in all 'search' command queries.
	`,
	Args: func(cmd *cobra.Command, args []string) error {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 2 {
			needsHelp = true
		}

		if needsHelp {
			return errors.New("must provide 1 or 2 arguments. See 'lhctl get externalEvent -h'")
		}

		return nil
	},
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		ctx := requestContext(cmd)

		littlehorse.PrintResp(getGlobalClient(cmd).GetCorrelatedEvent(
			ctx,
			&lhproto.CorrelatedEventId{
				Key:                args[0],
				ExternalEventDefId: &lhproto.ExternalEventDefId{Name: args[1]},
			},
		))
	},
}

var putCorrelatedEventCmd = &cobra.Command{
	Use:   "correlatedEvent <key> <externalEventName> [(<varType> <payload>)]",
	Short: "Put (Create or Update) a CorrelatedEvent.",
	Long: `
Create a CorrelatedEvent. If --guid is passed, you can also update a correlatedEvent.

The payload is deserialized according to the type. JSON objects should be provided as
a string; BYTES objects should be b64-encoded.

It's also possible to pass an empty input by excluding the last two arguments:
lhctl put correlatedEvent <key> <externalEventName>
`,
	Args: func(cmd *cobra.Command, args []string) error {
		if len(args) != 2 && len(args) != 4 {
			return errors.New("requires 2 or 4 args")
		}

		return nil
	},
	Run: func(cmd *cobra.Command, args []string) {
		key, eedName := args[0], args[1]
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

		req := lhproto.PutCorrelatedEventRequest{
			Key:                key,
			ExternalEventDefId: &lhproto.ExternalEventDefId{Name: eedName},
			Content:            content,
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).PutCorrelatedEvent(
				requestContext(cmd),
				&req,
			),
		)
	},
}

var deleteCorrelatedEventCmd = &cobra.Command{
	Use:   "correlatedEvent <key> <externalEventDefName>",
	Short: "Delete a CorrelatedEvent.",
	Args:  cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		key := args[0]
		externalEventDefName := args[1]
		littlehorse.PrintResp(getGlobalClient(cmd).DeleteCorrelatedEvent(
			requestContext(cmd),
			&lhproto.DeleteCorrelatedEventRequest{
				Id: &lhproto.CorrelatedEventId{
					Key: key,
					ExternalEventDefId: &lhproto.ExternalEventDefId{
						Name: externalEventDefName,
					},
				},
			},
		))
	},
}

func init() {
	getCmd.AddCommand(getCorrelatedEventCmd)
	putCmd.AddCommand(putCorrelatedEventCmd)
	deleteCmd.AddCommand(deleteCorrelatedEventCmd)
}
