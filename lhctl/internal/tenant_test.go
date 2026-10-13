package internal

import (
	"context"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/grpc"
)

type tenantClient struct {
	lhproto.LittleHorseClient
	putRequest    *lhproto.PutTenantRequest
	searchRequest *lhproto.SearchTenantRequest
}

func (c *tenantClient) PutTenant(_ context.Context, req *lhproto.PutTenantRequest, _ ...grpc.CallOption) (*lhproto.Tenant, error) {
	c.putRequest = req
	return &lhproto.Tenant{}, nil
}

func (c *tenantClient) SearchTenant(_ context.Context, req *lhproto.SearchTenantRequest, _ ...grpc.CallOption) (*lhproto.TenantIdList, error) {
	c.searchRequest = req
	return &lhproto.TenantIdList{}, nil
}

func TestTenantNameFlagPresence(t *testing.T) {
	previousClient, previousConfig := globalClient, globalConfig
	t.Cleanup(func() {
		globalClient, globalConfig = previousClient, previousConfig
		for _, cmd := range []*cobra.Command{putTenantCmd, searchTenantCmd} {
			_ = cmd.Flags().Set("name", "")
			cmd.Flags().Lookup("name").Changed = false
		}
	})
	fake := &tenantClient{}
	var client lhproto.LittleHorseClient = fake
	globalClient = &client
	globalConfig = &littlehorse.LHConfig{}

	for _, value := range []string{"", "Acme — Production"} {
		for _, changed := range []bool{false, true} {
			for _, cmd := range []*cobra.Command{putTenantCmd, searchTenantCmd} {
				_ = cmd.Flags().Set("name", value)
				cmd.Flags().Lookup("name").Changed = changed
			}
			putTenantCmd.Run(putTenantCmd, []string{"acme-prod"})
			searchTenantCmd.Run(searchTenantCmd, nil)
			for _, name := range []*string{fake.putRequest.Name, fake.searchRequest.Name} {
				if (name != nil) != changed || (name != nil && *name != value) {
					t.Fatalf("name presence or value lost: changed=%v value=%q name=%v", changed, value, name)
				}
			}
			if fake.putRequest.Id != "acme-prod" {
				t.Fatal("tenant must still be addressed by ID")
			}
		}
	}
}
