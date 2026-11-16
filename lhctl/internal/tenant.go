package internal

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

var putTenantCmd = &cobra.Command{
	Use:   "tenant <id>",
	Short: "Create or update a Tenant.",
	Long: `Create or update a Tenant. A name-only update fetches and resends the current output configuration;
if the Tenant does not exist, it is created with output disabled. This is not atomic: concurrent changes
can be overwritten. With no name or output flag, output is disabled.`,
	Args: cobra.ExactArgs(1),
	RunE: func(cmd *cobra.Command, args []string) error {
		allEvents, _ := cmd.Flags().GetBool("all-entity-events")
		noEvents, _ := cmd.Flags().GetBool("no-entity-events")
		disable, _ := cmd.Flags().GetBool("disable-output-topic")
		if (allEvents && noEvents) || (disable && (allEvents || noEvents)) {
			return fmt.Errorf("only one of --all-entity-events, --no-entity-events, and --disable-output-topic may be true")
		}
		putTenantReq := &lhproto.PutTenantRequest{
			Id: args[0],
		}

		if cmd.Flags().Changed("name") {
			name, _ := cmd.Flags().GetString("name")
			putTenantReq.Name = &name
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
		client := getGlobalClient(cmd)
		ctx := requestContext(cmd)
		if putTenantReq.Name != nil && !allEvents && !noEvents && !disable {
			tenant, err := client.GetTenant(ctx, &lhproto.TenantId{Id: args[0]})
			if err == nil {
				// Copy the complete config, including absence. This read/modify/write is not atomic.
				putTenantReq.OutputTopicConfig = tenant.OutputTopicConfig
			} else if status.Code(err) != codes.NotFound {
				return fmt.Errorf("cannot preserve output settings for tenant %q: %w; use an explicit output flag to replace settings", args[0], err)
			}
		}
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
	putTenantCmd.Flags().String("name", "", "Human-readable tenant name; an empty string clears it.")
	searchTenantCmd.Flags().String("name", "", "Return tenants whose name exactly matches this string.")
	putTenantCmd.Flags().Bool("all-entity-events", false, "Enable output for all eligible entities; false does not select this mode.")
	putTenantCmd.Flags().Bool("no-entity-events", false, "Enable output only for explicitly-enabled entities; false does not select this mode.")
	putTenantCmd.Flags().Bool("disable-output-topic", false, "Disable output without fetching existing settings; false does not select this mode.")
	searchCmd.AddCommand(searchTenantCmd)
	getCmd.AddCommand(getTenantCmd)
}
