package cmd

import (
	"log"
	"os"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

var getWfSpecCmd = &cobra.Command{
	Use:   "wfSpec <name> [<major version> [<minor version>]]",
	Short: "Get a WfSpec by Name and optionally Major Version and Revision.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfSpec to get.")
		}

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
			common.PrintResp(
				getGlobalClient(cmd).GetLatestWfSpec(
					requestContext(cmd),
					&model.GetLatestWfSpecRequest{
						Name:         name,
						MajorVersion: majorVersion,
					},
				),
			)
		} else {
			common.PrintResp(
				getGlobalClient(cmd).GetWfSpec(
					requestContext(cmd),
					&model.WfSpecId{
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
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the filename to deploy from.")
		}
		pws := &model.PutWfSpecRequest{}

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

		common.PrintResp(getGlobalClient(cmd).PutWfSpec(requestContext(cmd), pws))
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
	Run: func(cmd *cobra.Command, args []string) {
		name, _ := cmd.Flags().GetString("name")
		taskDef, _ := cmd.Flags().GetString("taskDef")
		prefix, _ := cmd.Flags().GetString("prefix")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		search := &model.SearchWfSpecRequest{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if name != "" {
			search.WfSpecCriteria = &model.SearchWfSpecRequest_Name{
				Name: name,
			}
		} else if prefix != "" {
			search.WfSpecCriteria = &model.SearchWfSpecRequest_Prefix{
				Prefix: prefix,
			}
		} else if taskDef != "" {
			search.WfSpecCriteria = &model.SearchWfSpecRequest_TaskDefName{
				TaskDefName: taskDef,
			}
		}
		// if none are set, then we don't set the WfSpecCriteria, and the server
		// interprets that as "just give me all the WfSpec's".

		common.PrintResp(
			getGlobalClient(cmd).SearchWfSpec(requestContext(cmd), search),
		)
	},
}

var deleteWfSpecCmd = &cobra.Command{
	Use:   "wfSpec <name> <version>",
	Short: "Delete a WfSpec.",
	Long: `Delete a WfSpec. You must provide the name and exact version of the
WfSpec to delete.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 3 {
			log.Fatal("You must provide three arguments: Name, Major Version, and Revision of WfSpec to Delete")
		}

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

		common.PrintResp(
			getGlobalClient(cmd).DeleteWfSpec(
				requestContext(cmd),
				&model.DeleteWfSpecRequest{
					Id: &model.WfSpecId{
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
