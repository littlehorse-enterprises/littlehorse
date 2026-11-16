package internal

import (
	"context"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/proto"
)

type putTenantClient struct {
	lhproto.LittleHorseClient
	request *lhproto.PutTenantRequest
	tenant  *lhproto.Tenant
	getErr  error
	putErr  error
	gets    int
	calls   []string
}

func (c *putTenantClient) PutTenant(_ context.Context, req *lhproto.PutTenantRequest, _ ...grpc.CallOption) (*lhproto.Tenant, error) {
	c.calls = append(c.calls, "put")
	c.request = req
	return &lhproto.Tenant{}, c.putErr
}

func (c *putTenantClient) GetTenant(_ context.Context, req *lhproto.TenantId, _ ...grpc.CallOption) (*lhproto.Tenant, error) {
	c.calls = append(c.calls, "get:"+req.Id)
	c.gets++
	if c.getErr != nil {
		return nil, c.getErr
	}
	if c.tenant == nil {
		return &lhproto.Tenant{}, nil
	}
	return c.tenant, nil
}

func resetPutTenantFlags(t *testing.T) {
	t.Helper()
	for _, name := range []string{"name", "all-entity-events", "no-entity-events", "disable-output-topic"} {
		flag := putTenantCmd.Flags().Lookup(name)
		if err := flag.Value.Set(flag.DefValue); err != nil {
			t.Fatal(err)
		}
		flag.Changed = false
	}
}

func runPutTenant(t *testing.T, args ...string) error {
	t.Helper()
	resetPutTenantFlags(t)
	defer resetPutTenantFlags(t)
	if err := putTenantCmd.ParseFlags(args); err != nil {
		return err
	}
	positional := putTenantCmd.Flags().Args()
	if err := putTenantCmd.ValidateArgs(positional); err != nil {
		return err
	}
	return putTenantCmd.RunE(putTenantCmd, positional)
}

func TestPutTenantCommand(t *testing.T) {
	previousClient, previousConfig := globalClient, globalConfig
	t.Cleanup(func() { globalClient, globalConfig = previousClient, previousConfig })
	name := "Acme"
	emptyName := ""
	all := &lhproto.OutputTopicConfig{DefaultRecordingLevel: lhproto.OutputTopicConfig_ALL_ENTITY_EVENTS}
	none := &lhproto.OutputTopicConfig{DefaultRecordingLevel: lhproto.OutputTopicConfig_NO_ENTITY_EVENTS}
	for _, tc := range []struct {
		name  string
		flags []string
		fetch bool
		want  *lhproto.PutTenantRequest
	}{
		{"omitted", nil, false, &lhproto.PutTenantRequest{Id: "acme"}},
		{"rename", []string{"--name=Acme"}, true, &lhproto.PutTenantRequest{Id: "acme", Name: &name}},
		{"clear name", []string{"--name="}, true, &lhproto.PutTenantRequest{Id: "acme", Name: &emptyName}},
		{"all events", []string{"--all-entity-events"}, false, &lhproto.PutTenantRequest{Id: "acme", OutputTopicConfig: all}},
		{"explicit entities", []string{"--no-entity-events"}, false, &lhproto.PutTenantRequest{Id: "acme", OutputTopicConfig: none}},
		{"replace and rename", []string{"--all-entity-events", "--name=Acme"}, false, &lhproto.PutTenantRequest{Id: "acme", Name: &name, OutputTopicConfig: all}},
		{"false all", []string{"--all-entity-events=false"}, false, &lhproto.PutTenantRequest{Id: "acme"}},
		{"false none", []string{"--no-entity-events=false"}, false, &lhproto.PutTenantRequest{Id: "acme"}},
		{"all false", []string{"--all-entity-events=false", "--no-entity-events=false"}, false, &lhproto.PutTenantRequest{Id: "acme"}},
		{"false all with none", []string{"--all-entity-events=false", "--no-entity-events"}, false, &lhproto.PutTenantRequest{Id: "acme", OutputTopicConfig: none}},
		{"disable", []string{"--disable-output-topic"}, false, &lhproto.PutTenantRequest{Id: "acme"}},
		{"disable and rename", []string{"--disable-output-topic", "--name=Acme"}, false, &lhproto.PutTenantRequest{Id: "acme", Name: &name}},
		{"false disable with all", []string{"--disable-output-topic=false", "--all-entity-events"}, false, &lhproto.PutTenantRequest{Id: "acme", OutputTopicConfig: all}},
		{"false mode with disable", []string{"--all-entity-events=false", "--disable-output-topic"}, false, &lhproto.PutTenantRequest{Id: "acme"}},
		{"disable conflicts with all", []string{"--disable-output-topic", "--all-entity-events"}, false, nil},
		{"disable conflicts with none", []string{"--disable-output-topic", "--no-entity-events"}, false, nil},
		{"conflicting modes", []string{"--all-entity-events", "--no-entity-events"}, false, nil},
	} {
		t.Run(tc.name, func(t *testing.T) {
			fake := &putTenantClient{}
			if !tc.fetch {
				fake.getErr = status.Error(codes.PermissionDenied, "must not fetch")
			}
			var client lhproto.LittleHorseClient = fake
			globalClient, globalConfig = &client, &littlehorse.LHConfig{}
			err := runPutTenant(t, append([]string{"acme"}, tc.flags...)...)
			if tc.want == nil {
				if err == nil || fake.request != nil || fake.gets != 0 {
					t.Fatalf("expected rejection before RPC, got error %v and request %v", err, fake.request)
				}
			} else if err != nil || !proto.Equal(tc.want, fake.request) {
				t.Fatalf("got request %v, error %v; want %v", fake.request, err, tc.want)
			}
		})
	}
}

type tenantClient struct {
	lhproto.LittleHorseClient
	putRequest    *lhproto.PutTenantRequest
	searchRequest *lhproto.SearchTenantRequest
}

func (c *tenantClient) PutTenant(_ context.Context, req *lhproto.PutTenantRequest, _ ...grpc.CallOption) (*lhproto.Tenant, error) {
	c.putRequest = req
	return &lhproto.Tenant{}, nil
}

func (c *tenantClient) GetTenant(_ context.Context, _ *lhproto.TenantId, _ ...grpc.CallOption) (*lhproto.Tenant, error) {
	return &lhproto.Tenant{}, nil
}

func (c *tenantClient) SearchTenant(_ context.Context, req *lhproto.SearchTenantRequest, _ ...grpc.CallOption) (*lhproto.TenantIdList, error) {
	c.searchRequest = req
	return &lhproto.TenantIdList{}, nil
}

func TestTenantNameFlagPresence(t *testing.T) {
	resetPutTenantFlags(t)
	t.Cleanup(func() { resetPutTenantFlags(t) })
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
			if err := putTenantCmd.RunE(putTenantCmd, []string{"acme-prod"}); err != nil {
				t.Fatal(err)
			}
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

func TestPutTenantPreservesFetchedConfig(t *testing.T) {
	previousClient, previousConfig := globalClient, globalConfig
	t.Cleanup(func() { globalClient, globalConfig = previousClient, previousConfig })
	all := &lhproto.OutputTopicConfig{DefaultRecordingLevel: lhproto.OutputTopicConfig_ALL_ENTITY_EVENTS}
	// Preserve fields introduced by a newer server, not just the known recording level.
	all.ProtoReflect().SetUnknown([]byte{0x10, 0x01})
	for _, config := range []*lhproto.OutputTopicConfig{nil, all, {DefaultRecordingLevel: lhproto.OutputTopicConfig_NO_ENTITY_EVENTS}} {
		for _, name := range []string{"Acme", ""} {
			fake := &putTenantClient{tenant: &lhproto.Tenant{OutputTopicConfig: config}}
			var client lhproto.LittleHorseClient = fake
			globalClient, globalConfig = &client, &littlehorse.LHConfig{}
			if err := runPutTenant(t, "acme", "--name="+name, "--all-entity-events=false", "--no-entity-events=false", "--disable-output-topic=false"); err != nil {
				t.Fatal(err)
			}
			if len(fake.calls) != 2 || fake.calls[0] != "get:acme" || fake.calls[1] != "put" {
				t.Fatalf("expected fetch before one write, got %v", fake.calls)
			}
			want := &lhproto.PutTenantRequest{Id: "acme", Name: &name, OutputTopicConfig: config}
			if !proto.Equal(fake.request, want) {
				t.Fatalf("got %v, want %v", fake.request, want)
			}
		}
	}
}

func TestPutTenantFetchFailurePreventsWrite(t *testing.T) {
	previousClient, previousConfig := globalClient, globalConfig
	t.Cleanup(func() { globalClient, globalConfig = previousClient, previousConfig })
	for _, code := range []codes.Code{codes.PermissionDenied, codes.Unavailable, codes.DeadlineExceeded} {
		t.Run(code.String(), func(t *testing.T) {
			fake := &putTenantClient{getErr: status.Error(code, "read failed")}
			var client lhproto.LittleHorseClient = fake
			globalClient, globalConfig = &client, &littlehorse.LHConfig{}
			err := runPutTenant(t, "acme", "--name=Acme")
			if status.Code(err) != code || fake.request != nil || fake.gets != 1 {
				t.Fatalf("expected read failure without write: error=%v request=%v reads=%d", err, fake.request, fake.gets)
			}
		})
	}
}

func TestPutTenantDoesNotRetryFailedWrite(t *testing.T) {
	previousClient, previousConfig := globalClient, globalConfig
	t.Cleanup(func() { globalClient, globalConfig = previousClient, previousConfig })
	fake := &putTenantClient{putErr: status.Error(codes.Unavailable, "write failed")}
	var client lhproto.LittleHorseClient = fake
	globalClient, globalConfig = &client, &littlehorse.LHConfig{}
	if err := runPutTenant(t, "acme", "--name=Acme"); status.Code(err) != codes.Unavailable {
		t.Fatalf("unexpected error: %v", err)
	}
	if len(fake.calls) != 2 {
		t.Fatalf("unexpected retries: %v", fake.calls)
	}
}

func TestPutTenantCreatesMissingTenantWithName(t *testing.T) {
	previousClient, previousConfig := globalClient, globalConfig
	t.Cleanup(func() { globalClient, globalConfig = previousClient, previousConfig })
	fake := &putTenantClient{getErr: status.Error(codes.NotFound, "no tenant")}
	var client lhproto.LittleHorseClient = fake
	globalClient, globalConfig = &client, &littlehorse.LHConfig{}
	if err := runPutTenant(t, "acme", "--name=Acme"); err != nil {
		t.Fatal(err)
	}
	name := "Acme"
	if want := (&lhproto.PutTenantRequest{Id: "acme", Name: &name}); !proto.Equal(fake.request, want) {
		t.Fatalf("got %v, want %v", fake.request, want)
	}
}
