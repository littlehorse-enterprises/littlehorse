/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package internal

import (
	"fmt"
	"log"
	"os"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

var deployExternalEventDefCmd = &cobra.Command{
	Use:   "externalEventDef <filename>",
	Short: "Create an ExternalEventDef from a JSON or Protobuf file.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		peed := &lhproto.PutExternalEventDefRequest{}

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

		littlehorse.PrintResp(getGlobalClient(cmd).PutExternalEventDef(requestContext(cmd), peed))
	},
}

// getExternalEventDefCmd represents the externalEventDef command
var getExternalEventDefCmd = &cobra.Command{
	Use:   "externalEventDef <name>",
	Short: "Get an ExternalEventDef by name.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(
			getGlobalClient(cmd).GetExternalEventDef(
				requestContext(cmd),
				&lhproto.ExternalEventDefId{
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

		littlehorse.PrintResp(
			getGlobalClient(cmd).SearchExternalEventDef(
				requestContext(cmd),
				&lhproto.SearchExternalEventDefRequest{
					Bookmark: bookmark,
					Limit:    &limit,
					Prefix:   &prefix,
				}),
		)
	},
}

var deleteExternalEventDefCmd = &cobra.Command{
	Use:   "externalEventDef <name>",
	Short: "Delete an ExternalEventDef.",
	Long: `Delete an ExternalEventDef. You must provide the name of the
ExternalEventDef to delete.
	`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		name := args[0]

		littlehorse.PrintResp(
			getGlobalClient(cmd).DeleteExternalEventDef(
				requestContext(cmd),
				&lhproto.DeleteExternalEventDefRequest{
					Id: &lhproto.ExternalEventDefId{
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
