package cmd

import (
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

var getUserTaskDefCmd = &cobra.Command{
	Use:   "userTaskDef <name>",
	Short: "Get a UserTaskDef by Name and optionally Version.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of UserTaskDef to get.")
		}

		rawNameStr := args[0]
		var name string
		var version int32
		hasVersion := false

		if strings.Contains(rawNameStr, "/") {
			name = strings.Split(rawNameStr, "/")[0]
			versionStr := strings.Split(rawNameStr, "/")[1]

			versionInt, err := strconv.ParseInt(versionStr, 10, 32)
			if err != nil {
				log.Fatal(err)
			}
			version = int32(versionInt)
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
				getGlobalClient(cmd).GetLatestUserTaskDef(
					requestContext(),
					&model.GetLatestUserTaskDefRequest{
						Name: name,
					},
				),
			)
		} else {
			common.PrintResp(
				getGlobalClient(cmd).GetUserTaskDef(
					requestContext(),
					&model.UserTaskDefId{
						Name:    name,
						Version: version,
					},
				),
			)
		}
	},
}

var deployUserTaskDefCmd = &cobra.Command{
	Use:   "userTaskDef <filename>",
	Short: "Deploy a userTaskDef from a JSON or Protobuf file.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the filename to deploy from.")
		}
		pws := &model.PutUserTaskDefRequest{}

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

		common.PrintResp(getGlobalClient(cmd).PutUserTaskDef(requestContext(), pws))
	},
}

var searchUserTaskDefCmd = &cobra.Command{
	Use:   "userTaskDef",
	Short: "Search for UserTaskDefs",
	Long: `Search for UserTaskDefs. You may provide any of the following option groups:
[name]
[prefix]
[taskDef]

If you provide no optional arguments, searches for all UserTaskDefs.

Returns a list of ObjectId's that can be passed into 'lhctl get userTaskDef'.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		name, _ := cmd.Flags().GetString("name")
		prefix, _ := cmd.Flags().GetString("prefix")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		search := &model.SearchUserTaskDefRequest{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if name != "" {
			search.UserTaskDefCriteria = &model.SearchUserTaskDefRequest_Name{
				Name: name,
			}
		} else if prefix != "" {
			search.UserTaskDefCriteria = &model.SearchUserTaskDefRequest_Prefix{
				Prefix: prefix,
			}
		}

		common.PrintResp(
			getGlobalClient(cmd).SearchUserTaskDef(requestContext(), search),
		)
	},
}

var deleteUserTaskDefCmd = &cobra.Command{
	Use:   "userTaskDef <name> <version>",
	Short: "Delete a UserTaskDef.",
	Long: `Delete a UserTaskDef. You must provide the name and exact version of the
UserTaskDef to delete.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 2 {
			log.Fatal("You must provide two arguments: Name and Version of UserTaskDef to Delete")
		}

		name := args[0]
		versionRaw := args[1]

		version, err := strconv.Atoi(versionRaw)
		if err != nil {
			log.Fatal("Couldn't convert version to int:", err)
		}

		common.PrintResp(
			getGlobalClient(cmd).DeleteUserTaskDef(
				requestContext(),
				&model.DeleteUserTaskDefRequest{
					Id: &model.UserTaskDefId{
						Name:    name,
						Version: int32(version),
					},
				}),
		)
	},
}

func init() {
	getUserTaskDefCmd.Flags().Int32("v", -1, "Optionally provide specific version.")
	getCmd.AddCommand(getUserTaskDefCmd)

	deployCmd.AddCommand(deployUserTaskDefCmd)
	searchCmd.AddCommand(searchUserTaskDefCmd)
	searchUserTaskDefCmd.Flags().String("name", "", "Name of UserTaskDefs to search for.")
	searchUserTaskDefCmd.Flags().String("prefix", "", "Prefix of name of UserTaskDefs to search for.")
	searchUserTaskDefCmd.MarkFlagsMutuallyExclusive("name", "prefix")

	deleteCmd.AddCommand(deleteUserTaskDefCmd)
}
