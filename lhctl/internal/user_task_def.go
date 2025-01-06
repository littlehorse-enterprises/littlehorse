package internal

import (
	"log"
	"os"
	"strconv"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

var getUserTaskDefCmd = &cobra.Command{
	Use:   "userTaskDef <name>",
	Short: "Get a UserTaskDef by Name and optionally Version.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
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
			littlehorse.PrintResp(
				getGlobalClient(cmd).GetLatestUserTaskDef(
					requestContext(cmd),
					&lhproto.GetLatestUserTaskDefRequest{
						Name: name,
					},
				),
			)
		} else {
			littlehorse.PrintResp(
				getGlobalClient(cmd).GetUserTaskDef(
					requestContext(cmd),
					&lhproto.UserTaskDefId{
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
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		pws := &lhproto.PutUserTaskDefRequest{}

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

		littlehorse.PrintResp(getGlobalClient(cmd).PutUserTaskDef(requestContext(cmd), pws))
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
	Args: cobra.ExactArgs(0),
	Run: func(cmd *cobra.Command, args []string) {
		name, _ := cmd.Flags().GetString("name")
		prefix, _ := cmd.Flags().GetString("prefix")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		search := &lhproto.SearchUserTaskDefRequest{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if name != "" {
			search.UserTaskDefCriteria = &lhproto.SearchUserTaskDefRequest_Name{
				Name: name,
			}
		} else if prefix != "" {
			search.UserTaskDefCriteria = &lhproto.SearchUserTaskDefRequest_Prefix{
				Prefix: prefix,
			}
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).SearchUserTaskDef(requestContext(cmd), search),
		)
	},
}

var deleteUserTaskDefCmd = &cobra.Command{
	Use:   "userTaskDef <name> <version>",
	Short: "Delete a UserTaskDef.",
	Long: `Delete a UserTaskDef. You must provide the name and exact version of the
UserTaskDef to delete.
	`,
	Args: cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		name := args[0]
		versionRaw := args[1]

		version, err := strconv.Atoi(versionRaw)
		if err != nil {
			log.Fatal("Couldn't convert version to int:", err)
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).DeleteUserTaskDef(
				requestContext(cmd),
				&lhproto.DeleteUserTaskDefRequest{
					Id: &lhproto.UserTaskDefId{
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
