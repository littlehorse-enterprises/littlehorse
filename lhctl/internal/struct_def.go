package internal

import (
	"log"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

var getStructDefCmd = &cobra.Command{
	Use:   "structDef <name> <version>",
	Short: "Get a StructDef by Name",
	Args:  cobra.MaximumNArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		name := args[0]
		version, err := strconv.Atoi(args[1])

		if err != nil {
			log.Fatal("Couldn't convert version to int:\n", err)
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).GetStructDef(
				requestContext(cmd),
				&lhproto.StructDefId{
					Name:    name,
					Version: int32(version),
				},
			),
		)
	},
}

var searchStructDefCmd = &cobra.Command{
	Use:   "structDef <prefix>",
	Short: "Search for StructDefs",
	Long: `Search for StructDefs.

Search for StructDefs according to a prefix.

Future support will be added for searching for all versions of an exact StructDef name;
	`,
	Args: cobra.MaximumNArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		searchCriteria := &lhproto.SearchStructDefRequest_Prefix{}

		if len(args) > 0 {
			searchCriteria.Prefix = args[0]
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).SearchStructDef(
				requestContext(cmd),
				&lhproto.SearchStructDefRequest{
					Bookmark:          bookmark,
					Limit:             &limit,
					StructDefCriteria: searchCriteria,
				}),
		)
	},
}

var deleteStructDefCmd = &cobra.Command{
	Use:   "structDef <name> <version>",
	Short: "Delete a StructDef.",
	Long: `Delete a StructDef. You must provide the name and version of the StructDef to delete.
	`,
	Args: cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {

		name := args[0]
		version, err := strconv.Atoi(args[1])

		if err != nil {
			log.Fatal("Couldn't convert version to int:\n", err)
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).DeleteStructDef(
				requestContext(cmd),
				&lhproto.DeleteStructDefRequest{
					Id: &lhproto.StructDefId{
						Name:    name,
						Version: int32(version),
					},
				}),
		)
	},
}

func init() {
	getCmd.AddCommand(getStructDefCmd)

	searchCmd.AddCommand(searchStructDefCmd)

	deleteCmd.AddCommand(deleteStructDefCmd)
}
