/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"fmt"
	"log"
	"os"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

var deployTaskDefCmd = &cobra.Command{
	Use:   "taskDef <filename>",
	Short: "Create a TaskDef from a JSON or Protobuf file.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the filename to deploy from.")

		}
		ptd := &model.PutTaskDefRequest{}

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
			fmt.Print("using proto")
			err = proto.Unmarshal(dat, ptd)
		} else {
			err = protojson.Unmarshal(dat, ptd)
		}
		if err != nil {
			log.Fatal("Failed reading deploy file: " + err.Error())

		}

		common.PrintResp(getGlobalClient(cmd).PutTaskDef(requestContext(cmd), ptd))
	},
}

// getTaskDefCmd represents the taskRun command
var getTaskDefCmd = &cobra.Command{
	Use:   "taskDef <name>",
	Short: "Get a TaskDef by Name",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the Name of TaskDef to get.")

		}

		name := args[0]
		common.PrintResp(
			getGlobalClient(cmd).GetTaskDef(
				requestContext(cmd),
				&model.TaskDefId{
					Name: name,
				},
			),
		)
	},
}

var searchTaskDefCmd = &cobra.Command{
	Use:   "taskDef",
	Short: "Search for TaskDefs",
	Long: `Search for TaskDefs.

No option groups for Search TaskDef are supported. Therefore, this command searches
for all TaskDefs.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		prefix, _ := cmd.Flags().GetString("prefix")

		common.PrintResp(
			getGlobalClient(cmd).SearchTaskDef(
				requestContext(cmd),
				&model.SearchTaskDefRequest{
					Bookmark: bookmark,
					Limit:    &limit,
					Prefix:   &prefix,
				}),
		)
	},
}

var deleteTaskDefCmd = &cobra.Command{
	Use:   "taskDef <name> <version>",
	Short: "Delete a TaskDef.",
	Long: `Delete a TaskDef. You must provide the name and of the TaskDef to delete.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: Name of TaskDef to Delete")

		}

		name := args[0]

		common.PrintResp(
			getGlobalClient(cmd).DeleteTaskDef(
				requestContext(cmd),
				&model.DeleteTaskDefRequest{
					Id: &model.TaskDefId{
						Name: name,
					},
				}),
		)
	},
}

func init() {
	getCmd.AddCommand(getTaskDefCmd)
	deployCmd.AddCommand(deployTaskDefCmd)

	searchCmd.AddCommand(searchTaskDefCmd)
	searchTaskDefCmd.Flags().String("prefix", "", "Prefix of name of TaskDefs to search for.")

	deleteCmd.AddCommand(deleteTaskDefCmd)
}
