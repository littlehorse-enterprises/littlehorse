package internal

import (
	"errors"
	"log"
	"strconv"
	"strings"

	"github.com/spf13/cobra"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

// getExternalEventCmd represents the externalEvent command
var getDataNuggetCmd = &cobra.Command{
	Use:   "dataNugget <key> <externalEventDefName> <guid>",
	Short: "Get a DataNugget by identifiers.",
	Long: `DataNugget's are identified uniquely by the combination of the following:
	- Key (Correlation ID)
	- ExternalEventDef name
	- A unique guid

	You may provide all three identifiers as three separate arguments or you may provide
	them delimited by the '/' character, as returned in all 'search' command queries.
	`,
	Args: func(cmd *cobra.Command, args []string) error {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 3 {
			needsHelp = true
		}

		if needsHelp {
			return errors.New("must provide 1 or 3 arguments. See 'lhctl get externalEvent -h'")
		}

		return nil
	},
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		ctx := requestContext(cmd)

		littlehorse.PrintResp(getGlobalClient(cmd).GetDataNugget(
			ctx,
			&lhproto.DataNuggetId{
				Key:                args[0],
				ExternalEventDefId: &lhproto.ExternalEventDefId{Name: args[1]},
				Guid:               args[2],
			},
		))
	},
}

var putDataNuggetCmd = &cobra.Command{
	Use:   "dataNugget <key> <externalEventName> [(<varType> <payload>)]",
	Short: "Put (Create or Update) a DataNugget.",
	Long: `
Create a DataNugget. If --guid is passed, you can also update a dataNugget.

The payload is deserialized according to the type. JSON objects should be provided as
a string; BYTES objects should be b64-encoded.

It's also possible to pass an empty input by excluding the last two arguments:
lhctl put dataNugget <key> <externalEventName>
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
		eedName, key := args[0], args[1]
		var guid *string = nil
		var epoch *int32 = nil
		if cmd.Flag("guid").Changed {
			guidTmp := cmd.Flag("guid").Value.String()
			guid = &guidTmp
		}
		if cmd.Flag("epoch").Changed {
			epochInt, e := strconv.Atoi(cmd.Flag("epoch").Value.String())
			if e != nil {
				log.Fatal(e)
			}
			tmpInt := int32(epochInt)
			epoch = &tmpInt
		}
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

		req := lhproto.PutDataNuggetRequest{
			Key:                key,
			ExternalEventDefId: &lhproto.ExternalEventDefId{Name: eedName},
			Content:            content,
			Guid:               guid,
			ExpectedEpoch:      epoch,
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).PutDataNugget(
				requestContext(cmd),
				&req,
			),
		)
	},
}

func init() {
	getCmd.AddCommand(getDataNuggetCmd)
	putCmd.AddCommand(putDataNuggetCmd)

	putDataNuggetCmd.Flags().Int("epoch", -1, "Expected epoch of the DataNugget")
	putDataNuggetCmd.Flags().String("guid", "", "Guid of the DataNugget. Helpful for idempotence.")
}
