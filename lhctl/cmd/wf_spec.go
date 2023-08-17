package cmd

import (
	"context"
	"log"
	"os"
	"strconv"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

var getWfSpecCmd = &cobra.Command{
	Use:   "wfSpecModel <name>",
	Short: "Get a WfSpec by Name and optionally Version.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfSpec to get.")
		}

		rawNameStr := args[0]
		var name string
		var version int32
		hasVersion := false

		if strings.Contains(rawNameStr, "/") {
			name = strings.Split(rawNameStr, "/")[0]
			versionStr := strings.Split(rawNameStr, "/")[1]

			vversionInt, err := strconv.ParseInt(versionStr, 10, 32)
			if err != nil {
				log.Fatal(err)
			}
			version = int32(vversionInt)
			hasVersion = true
		} else {
			name = rawNameStr
			versionRaw, _ := cmd.Flags().GetInt32("v")
			if versionRaw != -1 {
				version = versionRaw
				hasVersion = true
			}
		}

		if !hasVersion {
			common.PrintResp(
				getGlobalClient(cmd).GetLatestWfSpec(
					context.Background(),
					&model.GetLatestWfSpecPb{
						Name: name,
					},
				),
			)
		} else {
			common.PrintResp(
				getGlobalClient(cmd).GetWfSpec(
					context.Background(),
					&model.WfSpecId{
						Name:    name,
						Version: version,
					},
				),
			)
		}
	},
}

var deployWfSpecCmd = &cobra.Command{
	Use:   "wfSpecModel <filename>",
	Short: "Deploy a wfSpecModel from a JSON or Protobuf file.",
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

		common.PrintResp(getGlobalClient(cmd).PutWfSpec(context.Background(), pws))
	},
}

var searchWfSpecCmd = &cobra.Command{
	Use:   "wfSpecModel",
	Short: "Search for WfSpecs",
	Long: `Search for WfSpecs. You may provide any of the following option groups:
[name]
[prefix]
[taskDef]

If you provide no optional arguments, searches for all WfSpecs.

Returns a list of ObjectId's that can be passed into 'lhctl get wfSpecModel'.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		name, _ := cmd.Flags().GetString("name")
		taskDef, _ := cmd.Flags().GetString("taskDef")
		prefix, _ := cmd.Flags().GetString("prefix")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		search := &model.SearchWfSpecPb{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if name != "" {
			search.WfSpecCriteria = &model.SearchWfSpecPb_Name{
				Name: name,
			}
		} else if prefix != "" {
			search.WfSpecCriteria = &model.SearchWfSpecPb_Prefix{
				Prefix: prefix,
			}
		} else if taskDef != "" {
			search.WfSpecCriteria = &model.SearchWfSpecPb_TaskDefName{
				TaskDefName: taskDef,
			}
		}
		// if none are set, then we don't set the WfSpecCriteria, and the server
		// interprets that as "just give me all the WfSpec's".

		common.PrintResp(
			getGlobalClient(cmd).SearchWfSpec(context.Background(), search),
		)
	},
}

var deleteWfSpecCmd = &cobra.Command{
	Use:   "wfSpecModel <name> <version>",
	Short: "Delete a WfSpec.",
	Long: `Delete a WfSpec. You must provide the name and exact version of the
WfSpec to delete.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 2 {
			log.Fatal("You must provide two arguments: Name and Version of WfSpec to Delete")
		}

		name := args[0]
		versionRaw := args[1]

		version, err := strconv.Atoi(versionRaw)
		if err != nil {
			log.Fatal("Couldn't convert version to int: ", err)
		}

		common.PrintResp(
			getGlobalClient(cmd).DeleteWfSpec(
				context.Background(),
				&model.DeleteWfSpecPb{
					Name:    name,
					Version: int32(version),
				}),
		)
	},
}

func init() {
	getWfSpecCmd.Flags().Int32("v", -1, "Optionally provide specific version.")
	getCmd.AddCommand(getWfSpecCmd)

	deployCmd.AddCommand(deployWfSpecCmd)
	searchCmd.AddCommand(searchWfSpecCmd)
	searchWfSpecCmd.Flags().String("name", "", "Name of WfSpecs to search for.")
	searchWfSpecCmd.Flags().String("taskDef", "", "Name of TaskDef to search for.")
	searchWfSpecCmd.Flags().String("prefix", "", "Prefix of name of WfSpecs to search for.")
	searchWfSpecCmd.MarkFlagsMutuallyExclusive("name", "taskDef", "prefix")

	deleteCmd.AddCommand(deleteWfSpecCmd)
}
