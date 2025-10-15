package internal

import (
	"log"
	"os"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

var getWfSpecCmd = &cobra.Command{
	Use:   "wfSpec <name>",
	Short: "Get a WfSpec by Name and optionally Major Version and Revision.",
	Args:  cobra.MinimumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		name := args[0]

		var majorVersion *int32
		if raw, _ := cmd.Flags().GetInt32("majorVersion"); raw == -1 {
			majorVersion = nil
		} else {
			majorVersion = &raw
		}

		var revision *int32
		if raw, _ := cmd.Flags().GetInt32("revision"); raw == -1 {
			revision = nil
		} else {
			revision = &raw
		}

		if majorVersion == nil || revision == nil {
			littlehorse.PrintResp(
				getGlobalClient(cmd).GetLatestWfSpec(
					requestContext(cmd),
					&lhproto.GetLatestWfSpecRequest{
						Name:         name,
						MajorVersion: majorVersion,
					},
				),
			)
		} else {
			littlehorse.PrintResp(
				getGlobalClient(cmd).GetWfSpec(
					requestContext(cmd),
					&lhproto.WfSpecId{
						Name:         name,
						MajorVersion: *majorVersion,
						Revision:     *revision,
					},
				),
			)
		}
	},
}

var deployWfSpecCmd = &cobra.Command{
	Use:   "wfSpec <filename>",
	Short: "Deploy a wfSpec from a JSON or Protobuf file.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		pws := &lhproto.PutWfSpecRequest{}

		// First, read the file
		dat, err := os.ReadFile(args[0])
		if err != nil {
			log.Fatal("Failed to read file: ", err)
		}

		useProto, err := cmd.Flags().GetBool("proto")
		if err != nil {
			log.Fatal("Unexpected error: ", err)
		}
		if useProto {
			err = proto.Unmarshal(dat, pws)
		} else {
			err = protojson.Unmarshal(dat, pws)
		}
		if err != nil {
			log.Fatal("Failed reading deploy file: " + err.Error())
		}

		littlehorse.PrintResp(getGlobalClient(cmd).PutWfSpec(requestContext(cmd), pws))
	},
}

var searchWfSpecCmd = &cobra.Command{
	Use:   "wfSpec",
	Short: "Search for WfSpecs",
	Long: `Search for WfSpecs. You may provide any of the following option groups:
[name]
[prefix]
[taskDef]

If you provide no optional arguments, searches for all WfSpecs.

Returns a list of ObjectId's that can be passed into 'lhctl get wfSpec'.
	`,
	Args: cobra.ExactArgs(0),
	Run: func(cmd *cobra.Command, args []string) {
		name, _ := cmd.Flags().GetString("name")
		taskDef, _ := cmd.Flags().GetString("taskDef")
		prefix, _ := cmd.Flags().GetString("prefix")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		search := &lhproto.SearchWfSpecRequest{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if name != "" {
			search.WfSpecCriteria = &lhproto.SearchWfSpecRequest_Name{
				Name: name,
			}
		} else if prefix != "" {
			search.WfSpecCriteria = &lhproto.SearchWfSpecRequest_Prefix{
				Prefix: prefix,
			}
		} else if taskDef != "" {
			search.WfSpecCriteria = &lhproto.SearchWfSpecRequest_TaskDefName{
				TaskDefName: taskDef,
			}
		}
		// if none are set, then we don't set the WfSpecCriteria, and the server
		// interprets that as "just give me all the WfSpec's".

		littlehorse.PrintResp(
			getGlobalClient(cmd).SearchWfSpec(requestContext(cmd), search),
		)
	},
}

var deleteWfSpecCmd = &cobra.Command{
	Use:   "wfSpec <name> <majorVersionNumber> <revisionNumber>",
	Short: "Delete a WfSpec.",
	Long: `Delete a WfSpec. You must provide the name and exact version of the
WfSpec to delete.
	`,
	Args: cobra.ExactArgs(3),
	Run: func(cmd *cobra.Command, args []string) {
		name := args[0]
		majorVersionRaw := args[1]
		revisionRaw := args[2]

		majorVersion, err := strconv.Atoi(majorVersionRaw)
		if err != nil {
			log.Fatal("Couldn't convert major version to int: ", err)
		}
		revision, err := strconv.Atoi(revisionRaw)
		if err != nil {
			log.Fatal("Couldn't convert revision to int: ", err)
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).DeleteWfSpec(
				requestContext(cmd),
				&lhproto.DeleteWfSpecRequest{
					Id: &lhproto.WfSpecId{
						Name:         name,
						MajorVersion: int32(majorVersion),
						Revision:     int32(revision),
					},
				},
			),
		)
	},
}

func init() {
	getWfSpecCmd.Flags().Int32("majorVersion", -1, "Set specific WfSpec Major Version to get")
	getWfSpecCmd.Flags().Int32("revision", -1, "Set specific WfSpec Revision to get")
	getCmd.AddCommand(getWfSpecCmd)

	deployCmd.AddCommand(deployWfSpecCmd)
	searchCmd.AddCommand(searchWfSpecCmd)
	searchWfSpecCmd.Flags().String("name", "", "Name of WfSpecs to search for.")
	searchWfSpecCmd.Flags().String("taskDef", "", "Name of TaskDef to search for.")
	searchWfSpecCmd.Flags().String("prefix", "", "Prefix of name of WfSpecs to search for.")
	searchWfSpecCmd.MarkFlagsMutuallyExclusive("name", "taskDef", "prefix")

	deleteCmd.AddCommand(deleteWfSpecCmd)
}
