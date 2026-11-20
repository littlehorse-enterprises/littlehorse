package internal

import (
	"bufio"
	"fmt"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

var putTenantCmd = &cobra.Command{
	Use:   "tenant <id>",
	Short: "Create or update a Tenant.",
	Long: `Create or update a Tenant using all the supplied settings. Retype every setting you want to keep.
Omitting --name clears the name. Omitting both output flags disables output.
You must confirm before the request is sent; existing settings are not fetched or preserved.`,
	Args: cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		allEvents, _ := cmd.Flags().GetBool("all-entity-events")
		noEvents, _ := cmd.Flags().GetBool("no-entity-events")
		if allEvents && noEvents {
			return fmt.Errorf("only one of --all-entity-events and --no-entity-events may be true")
		}
		name, _ := cmd.Flags().GetString("name")
		putTenantReq := &lhproto.PutTenantRequest{
			Id:   args[0],
			Name: &name,
		}
		if allEvents {
			putTenantReq.OutputTopicConfig = &lhproto.OutputTopicConfig{
				DefaultRecordingLevel: lhproto.OutputTopicConfig_ALL_ENTITY_EVENTS,
			}
		} else if noEvents {
			putTenantReq.OutputTopicConfig = &lhproto.OutputTopicConfig{
				DefaultRecordingLevel: lhproto.OutputTopicConfig_NO_ENTITY_EVENTS,
			}
		}
		output := "Disabled"
		if allEvents {
			output = "Enabled — all eligible entities"
		} else if noEvents {
			output = "Enabled — explicitly enabled entities only"
		}
		displayName := fmt.Sprintf("%q", name)
		if name == "" {
			displayName = "(none; clears any existing display name)"
		}
		fmt.Fprintf(cmd.ErrOrStderr(), "Apply the following tenant settings?\n\n  \x1b[1mTenant ID:\x1b[0m %q\n  \x1b[1mDisplay Name:\x1b[0m %s\n  \x1b[1mOutput Topic Configuration:\x1b[0m %s\n\nThis will overwrite these settings if the tenant already exists.\nContinue? [y/N] ", args[0], displayName, output)
		answer, err := bufio.NewReader(cmd.InOrStdin()).ReadString('\n')
		if err != nil {
			return fmt.Errorf("tenant not updated: could not read confirmation: %w", err)
		}
		switch strings.ToLower(strings.TrimSpace(answer)) {
		case "y", "yes":
		default:
			return fmt.Errorf("tenant not updated: confirmation declined")
		}
		client := getGlobalClient(cmd)
		ctx := requestContext(cmd)
		response, err := client.PutTenant(ctx, putTenantReq)
		if err != nil {
			return err
		}
		littlehorse.PrintResp(response, nil)
		return nil
	},
}

var searchTenantCmd = &cobra.Command{
	Use:   "tenant",
	Short: "Search for all available TenantIds for current Principal",
	Args:  cobra.ExactArgs(0),
	Run: func(cmd *cobra.Command, args []string) {
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		req := &lhproto.SearchTenantRequest{Bookmark: bookmark, Limit: &limit}
		if cmd.Flags().Changed("name") {
			name, _ := cmd.Flags().GetString("name")
			req.Name = &name
		}
		littlehorse.PrintResp(getGlobalClient(cmd).SearchTenant(
			requestContext(cmd),
			req,
		))
	},
}

var getTenantCmd = &cobra.Command{
	Use:   "tenant <id>",
	Short: "Get a Tenant",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).GetTenant(
			requestContext(cmd),
			&lhproto.TenantId{
				Id: args[0],
			},
		))
	},
}

func init() {
	putCmd.AddCommand(putTenantCmd)
	putTenantCmd.Flags().String("name", "", "Human-readable tenant name; omitted or empty clears it.")
	searchTenantCmd.Flags().String("name", "", "Return tenants whose name exactly matches this string.")
	putTenantCmd.Flags().Bool("all-entity-events", false, "Enable output for all eligible entities; false does not select this mode.")
	putTenantCmd.Flags().Bool("no-entity-events", false, "Enable output only for explicitly-enabled entities; false does not select this mode.")
	searchCmd.AddCommand(searchTenantCmd)
	getCmd.AddCommand(getTenantCmd)
}
