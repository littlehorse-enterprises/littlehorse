package internal

import (
	"fmt"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
)

var putVariableCmd = &cobra.Command{
	Use:   "variable <wfRunId> <threadRunNumber> <varName> [(<varType> <payload>)]",
	Short: "Replace an existing variable's value.",
	Long: `Replace the value of an existing variable and attempt to advance its workflow.
Prints the previous value, respecting the variable's masking configuration.
Supported types: INT, STR, BYTES, BOOL, JSON_OBJ, JSON_ARR, DOUBLE, WF_RUN_ID, TIMESTAMP.
Quote JSON payloads; BYTES uses unpadded base64. TIMESTAMP accepts epoch milliseconds
or RFC 3339. Omit both varType and payload to explicitly set the variable to null.
The prototype does not validate the value against the variable's declared type.`,
	Example: `  lhctl put variable <wfRunId> 0 counter INT 42
  lhctl put variable <wfRunId> 0 message STR "hello"
  lhctl put variable <wfRunId> 0 data JSON_OBJ '{"enabled":true}'
  lhctl put variable <wfRunId> 0 value`,
	Args: func(cmd *cobra.Command, args []string) error {
		_, err := parsePutVariableRequest(args)
		return err
	},
	RunE: func(cmd *cobra.Command, args []string) error {
		req, err := parsePutVariableRequest(args)
		if err != nil {
			return err
		}
		resp, err := getGlobalClient(cmd).PutVariable(requestContext(cmd), req)
		if err != nil {
			return err
		}
		littlehorse.PrintResp(resp, nil)
		return nil
	},
}

func parsePutVariableRequest(args []string) (*lhproto.PutVariableRequest, error) {
	if len(args) != 3 && len(args) != 5 {
		return nil, fmt.Errorf("requires 3 or 5 arguments: provide both varType and payload, or omit both for null")
	}
	if args[0] == "" || args[2] == "" {
		return nil, fmt.Errorf("wfRunId and varName must not be empty")
	}
	threadRunNumber, err := strconv.ParseInt(args[1], 10, 32)
	if err != nil || threadRunNumber < 0 {
		return nil, fmt.Errorf("threadRunNumber must be a non-negative 32-bit integer")
	}
	value := &lhproto.VariableValue{}
	if len(args) == 5 {
		varType, ok := lhproto.VariableType_value[args[3]]
		if !ok {
			return nil, fmt.Errorf("unrecognized varType %q", args[3])
		}
		value, err = littlehorse.StrToVarVal(args[4], lhproto.VariableType(varType))
		if err != nil {
			return nil, fmt.Errorf("invalid payload: %w", err)
		}
		if value.GetValue() == nil {
			return nil, fmt.Errorf("unsupported varType %q", args[3])
		}
	}
	return &lhproto.PutVariableRequest{
		Id: &lhproto.VariableId{
			WfRunId:         littlehorse.StrToWfRunId(args[0]),
			ThreadRunNumber: int32(threadRunNumber),
			Name:            args[2],
		},
		Value: value,
	}, nil
}

func init() {
	putCmd.AddCommand(putVariableCmd)
}
