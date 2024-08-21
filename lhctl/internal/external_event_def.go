/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package internal

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

var deployExternalEventDefCmd = &cobra.Command{
	Use:   "externalEventDef <filename>",
	Short: "Create an ExternalEventDef from a JSON or Protobuf file.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the filename to deploy from.")

		}
		peed := &model.PutExternalEventDefRequest{}

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
			err = proto.Unmarshal(dat, peed)
		} else {
			err = protojson.Unmarshal(dat, peed)
		}
		if err != nil {
			log.Fatal("Failed reading deploy file: " + err.Error())

		}

		common.PrintResp(getGlobalClient(cmd).PutExternalEventDef(requestContext(cmd), peed))
	},
}

// getExternalEventDefCmd represents the externalEventDef command
var getExternalEventDefCmd = &cobra.Command{
	Use:   "externalEventDef <name>",
	Short: "Get an ExternalEventDef by name.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the Name of ExternalEventDef to get.")

		}

		common.PrintResp(
			getGlobalClient(cmd).GetExternalEventDef(
				requestContext(cmd),
				&model.ExternalEventDefId{
					Name: args[0],
				},
			),
		)
	},
}

var searchExternalEventDefCmd = &cobra.Command{
	Use:   "externalEventDef",
	Short: "Search for ExternalEventDef",
	Long: `Search for ExternalEventDefs.

No option groups for Search ExternalEventDef are supported. Therefore, this command
searches for all ExternalEventDefs.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		prefix, _ := cmd.Flags().GetString("prefix")

		common.PrintResp(
			getGlobalClient(cmd).SearchExternalEventDef(
				requestContext(cmd),
				&model.SearchExternalEventDefRequest{
					Bookmark: bookmark,
					Limit:    &limit,
					Prefix:   &prefix,
				}),
		)
	},
}

var deleteExternalEventDefCmd = &cobra.Command{
	Use:   "externalEventDef <name> <version>",
	Short: "Delete a ExternalEventDef.",
	Long: `Delete a ExternalEventDef. You must provide the name of the
ExternalEventDef to delete.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: Name of ExternalEventDef to Delete")

		}

		name := args[0]

		common.PrintResp(
			getGlobalClient(cmd).DeleteExternalEventDef(
				requestContext(cmd),
				&model.DeleteExternalEventDefRequest{
					Id: &model.ExternalEventDefId{
						Name: name,
					},
				}),
		)
	},
}

func init() {
	getCmd.AddCommand(getExternalEventDefCmd)
	deployCmd.AddCommand(deployExternalEventDefCmd)

	searchCmd.AddCommand(searchExternalEventDefCmd)
	searchExternalEventCmd.Flags().String("prefix", "", "Prefix of name of ExternalEventDefs to search for.")

	deleteCmd.AddCommand(deleteExternalEventDefCmd)
}
