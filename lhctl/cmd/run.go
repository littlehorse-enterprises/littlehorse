package cmd

import (
	"context"
	"log"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

// runCmd represents the run command
var runCmd = &cobra.Command{
	Use:   "run <wfSpecName> <<var1 name>> <<var1 val>>...",
	Short: "Run an instance of a WfSpec with provided Name and Input Variables.",
	Long: `
Run a workflow. You may optionally specify the ID for the WfRun and you may specify
input variables as well.

All positional arguments after the WfSpec Id or Name are interpreted as pairs of
{Variable Name, Variable Value}. The variable values are intelligently deserialized
to their appropriate types; for example, if var 'foo' is of type 'JSON_OBJ', then
the argument '{"bar":"baz"}' will be unmarshalled as a JSON object.

The previous example appears as follows in context:

lhctl run my_workflow_id foo '{"bar":"baz"}'
`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) < 1 {
			log.Fatal("You must provide at least 1 arg: WfSpec Id.")
		}

		runReq := &model.RunWfPb{}

		wfSpecName := args[0]
		runReq.WfSpecName = wfSpecName

		var wfSpecVersion *int32
		if raw, _ := cmd.Flags().GetInt32("wfSpecVersion"); raw == -1 {
			wfSpecVersion = nil
		} else {
			wfSpecVersion = &raw
		}
		runReq.WfSpecVersion = wfSpecVersion

		wfRunId, _ := cmd.Flags().GetString("wfRunId")
		if wfRunId != "" {
			runReq.Id = &wfRunId
		}

		// Now parse variables
		if len(args) > 1 {
			if len(args)%2 != 1 {
				log.Fatal(`
If you provide variables, you must provide pairs of <name, val>.
Therefore, you must have an even number of args after the WfSpec Id, for an
odd total number of args. See 'lhctl run --help' for details.`)
			}

			// Now, we need to look up the wfSpec and serialize the variables.
			var wfSpecReply *model.GetWfSpecReplyPb
			var err error

			if wfSpecVersion == nil {
				wfSpecReply, err = getGlobalClient(cmd).GetLatestWfSpec(
					context.Background(),
					&model.GetLatestWfSpecPb{Name: args[0]},
				)
			} else {
				wfSpecReply, err = getGlobalClient(cmd).GetWfSpec(
					context.Background(),
					&model.WfSpecIdPb{
						Name:    args[0],
						Version: *wfSpecVersion,
					})
			}

			if err != nil {
				log.Fatal("Unable to find WfSpec: " + err.Error())
			}
			if wfSpecReply.Code != model.LHResponseCodePb_OK {
				msg := "Unable to find WfSpec"
				if wfSpecReply.Message != nil {
					msg += ": " + *wfSpecReply.Message
				}
				log.Fatal(msg)
			}

			wfSpec := wfSpecReply.Result
			runReq.Variables = make(map[string]*model.VariableValuePb)
			varDefs := common.GetInputVarDefs(wfSpec)

			for i := 1; i+1 < len(args); i += 2 {
				varName := args[i]
				varValStr := args[i+1]

				varDef := varDefs[varName]
				if varDef == nil {
					log.Fatal("Variable name '" + varName + "' not found in WfSpec.")
				}

				runReq.Variables[varName], err = common.StrToVarVal(
					varValStr, varDef.Type,
				)

				if err != nil {
					log.Fatal("Failed converting variable: " + err.Error())
				}
			}
		}

		// At this point, we've loaded everything up, time to fire away.
		common.PrintResp(getGlobalClient(cmd).RunWf(context.Background(), runReq))
	},
}

func init() {
	runCmd.Flags().String("wfRunId", "", "Set the id of the WfRun (for idempotence)")
	runCmd.Flags().Int32("wfSpecVersion", -1, "Set specific WfSpec version to run")
	rootCmd.AddCommand(runCmd)
}
