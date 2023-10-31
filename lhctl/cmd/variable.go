/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"log"
	"strconv"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

// getVariableCmd represents the variable command
var getVariableCmd = &cobra.Command{
	Use:   "variable <wfRunId> <threadRunNumber> <varName>",
	Short: "Get a VariableValue by identifiers.",
	Long: `VariableValues's are identified uniquely by the combination of the following:
	- Associated WfRun Id
	- Thread Run Number
	- Variable Name

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
			log.Fatal("Must provide 1 or 3 arguments. See 'lhctl get variable -h'")

		}

		threadRunNumber, err := strconv.Atoi(args[1])
		if err != nil {
			log.Fatal("Failed parsing threadRunNumber" + err.Error())

		}

		common.PrintResp(
			getGlobalClient(cmd).GetVariable(
				requestContext(),
				&model.VariableId{
					WfRunId:         args[0],
					ThreadRunNumber: int32(threadRunNumber),
					Name:            args[2],
				},
			),
		)
	},
}

var searchVariableCmd = &cobra.Command{
	Use:   "variable",
	Short: "Search for Variables by WfRunId",
	Long: `
Search for Variable's by providing the WfRunId OR by specifying the name, type, and
value of variable to search for.

Returns a list of ObjectId's that can be passed into 'lhctl get variable'.

Choose one of the following option groups:
[wfRunId]
[varType, value, name, wfSpecName, wfSpecVersion]
`,
	Run: func(cmd *cobra.Command, args []string) {
		token, _ := cmd.Flags().GetString("token")
		if token != "" {
			log.Fatal("TODO: Support tokens")

		}

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		var search model.SearchVariableRequest
		wfRunId, _ := cmd.Flags().GetString("wfRunId")
		name, _ := cmd.Flags().GetString("name")
		varTypeStr, _ := cmd.Flags().GetString("varType")
		valueStr, _ := cmd.Flags().GetString("value")
		wfSpecName, _ := cmd.Flags().GetString("wfSpecName")
		wfSpecVersion, _ := cmd.Flags().GetInt32("wfSpecVersion")

		var wfSpecVersionPtr *int32
		if wfSpecVersion == -1 {
			wfSpecVersionPtr = nil
		} else {
			wfSpecVersionPtr = &wfSpecVersion
		}

		if wfRunId != "" {
			search = model.SearchVariableRequest{
				VariableCriteria: &model.SearchVariableRequest_WfRunId{
					WfRunId: wfRunId,
				},
			}
		} else {
			varType, validVarType := model.VariableType_value[varTypeStr]
			if !validVarType {
				log.Fatal(
					"Unrecognized varType. Valid options: INT, STR, BYTES, BOOL, JSON_OBJ, JSON_ARR, DOUBLE.",
				)

			}
			varTypeEnum := model.VariableType(varType)
			content, err := common.StrToVarVal(valueStr, varTypeEnum)
			if err != nil {
				log.Fatal("Failed deserializing payload: " + err.Error())

			}

			search = model.SearchVariableRequest{
				VariableCriteria: &model.SearchVariableRequest_Value{
					Value: &model.SearchVariableRequest_NameAndValueRequest{
						Value:         content,
						VarName:       name,
						WfSpecVersion: wfSpecVersionPtr,
						WfSpecName:    wfSpecName,
					},
				},
			}
		}

		search.Bookmark = bookmark
		search.Limit = &limit

		common.PrintResp(
			getGlobalClient(cmd).SearchVariable(requestContext(), &search),
		)
	},
}

var listVariableCmd = &cobra.Command{
	Use:   "variable <wfRunId>",
	Short: "List all Variable's for a given WfRun Id.",
	Long: `
Lists all Variable's for a given WfRun Id.
`,
	Run: func(cmd *cobra.Command, args []string) {
		// bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		// limit, _ := cmd.Flags().GetInt32("limit")

		if len(args) != 1 {
			log.Fatal("Must provide one arg: the WfRun ID!")
		}
		wfRunId := args[0]

		req := &model.ListVariablesRequest{
			WfRunId: wfRunId,
		}

		common.PrintResp(getGlobalClient(cmd).ListVariables(
			requestContext(),
			req,
		))
	},
}

func init() {
	getCmd.AddCommand(getVariableCmd)
	searchCmd.AddCommand(searchVariableCmd)
	listCmd.AddCommand(listVariableCmd)

	searchVariableCmd.Flags().String("wfRunId", "", "wfRunId of variables to look for.")
	searchVariableCmd.Flags().String("varType", "", "type of Variable you're searching for")
	searchVariableCmd.Flags().String("value", "", "value of variable to search for")
	searchVariableCmd.Flags().String("name", "", "name of the variable to search for")
	searchVariableCmd.Flags().String("wfSpecName", "", "name of WfSpec")
	searchVariableCmd.Flags().Int32("wfSpecVersion", -1, "WfSpecVersion to search for")

	searchVariableCmd.MarkFlagsRequiredTogether("name", "value", "varType", "wfSpecName")
}
