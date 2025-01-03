package internal

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

// runCmd represents the run command
var runCmd = &cobra.Command{
	Use:   "run <wfSpecName> [(<var1 name> <var1 val>)]...",
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
	Args: cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		runReq := &lhproto.RunWfRequest{}

		wfSpecName := args[0]
		runReq.WfSpecName = wfSpecName

		var majorVersion *int32
		if raw, _ := cmd.Flags().GetInt32("majorVersion"); raw == -1 {
			majorVersion = nil
		} else {
			majorVersion = &raw
		}
		runReq.MajorVersion = majorVersion

		var revision *int32
		if raw, _ := cmd.Flags().GetInt32("revision"); raw == -1 {
			revision = nil
		} else {
			revision = &raw
		}
		runReq.Revision = revision

		wfRunId, _ := cmd.Flags().GetString("wfRunId")
		if wfRunId != "" {
			runReq.Id = &wfRunId
		}

		parentWfRunId, _ := cmd.Flags().GetString("parentWfRunId")
		if parentWfRunId != "" {
			parentId := &lhproto.WfRunId{}
			parentId.Id = parentWfRunId
			runReq.ParentWfRunId = parentId
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
			var wfSpec *lhproto.WfSpec
			var err error

			if revision == nil {
				wfSpec, err = getGlobalClient(cmd).GetLatestWfSpec(
					requestContext(cmd),
					&lhproto.GetLatestWfSpecRequest{
						Name:         args[0],
						MajorVersion: majorVersion,
					},
				)
			} else {
				wfSpec, err = getGlobalClient(cmd).GetWfSpec(
					requestContext(cmd),
					&lhproto.WfSpecId{
						Name:         args[0],
						MajorVersion: *majorVersion,
						Revision:     *revision,
					})
			}

			if err != nil {
				log.Fatal("Unable to find WfSpec: " + err.Error())
			}

			runReq.Variables = make(map[string]*lhproto.VariableValue)
			varDefs := littlehorse.GetInputVarDefs(wfSpec)

			for i := 1; i+1 < len(args); i += 2 {
				varName := args[i]
				varValStr := args[i+1]

				varDef := varDefs[varName]
				if varDef == nil {
					log.Fatal("Variable name '" + varName + "' not found in WfSpec.")
				}

				runReq.Variables[varName], err = littlehorse.StrToVarVal(
					varValStr, varDef.Type,
				)

				if err != nil {
					log.Fatal("Failed converting variable: " + err.Error())
				}
			}
		}

		// At this point, we've loaded everything up, time to fire away.
		littlehorse.PrintResp(getGlobalClient(cmd).RunWf(requestContext(cmd), runReq))
	},
}

func init() {
	runCmd.Flags().String("wfRunId", "", "Set the id of the WfRun (for idempotence)")
	runCmd.Flags().String("parentWfRunId", "", "Set the ID of the parent WfRun")
	runCmd.Flags().Int32("majorVersion", -1, "Set specific WfSpec Major Version to run")
	runCmd.Flags().Int32("revision", -1, "Set specific WfSpec Revision to run")
	rootCmd.AddCommand(runCmd)
}
