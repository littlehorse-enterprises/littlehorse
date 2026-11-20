package internal

import (
	"bytes"
	"context"
	"strings"
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
	for _, name := range []string{"name", "all-entity-events", "no-entity-events"} {
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
	for _, tc := range []struct {
		name        string
		flags       []string
		level       *lhproto.OutputTopicConfig
		displayName string
		invalid     bool
	}{
		{name: "omitted"},
		{name: "name only disables output", flags: []string{"--name=Acme"}, displayName: "Acme"},
		{name: "empty name", flags: []string{"--name="}},
		{name: "all events", flags: []string{"--all-entity-events"}, level: &lhproto.OutputTopicConfig{DefaultRecordingLevel: lhproto.OutputTopicConfig_ALL_ENTITY_EVENTS}},
		{name: "explicit entities", flags: []string{"--no-entity-events"}, level: &lhproto.OutputTopicConfig{DefaultRecordingLevel: lhproto.OutputTopicConfig_NO_ENTITY_EVENTS}},
		{name: "all settings", flags: []string{"--name=Acme", "--all-entity-events"}, displayName: "Acme", level: &lhproto.OutputTopicConfig{DefaultRecordingLevel: lhproto.OutputTopicConfig_ALL_ENTITY_EVENTS}},
		{name: "false flags", flags: []string{"--all-entity-events=false", "--no-entity-events=false"}},
		{name: "false with true", flags: []string{"--all-entity-events=false", "--no-entity-events"}, level: &lhproto.OutputTopicConfig{DefaultRecordingLevel: lhproto.OutputTopicConfig_NO_ENTITY_EVENTS}},
		{name: "conflict", flags: []string{"--all-entity-events", "--no-entity-events"}, invalid: true},
	} {
		t.Run(tc.name, func(t *testing.T) {
			fake := &putTenantClient{}
			var client lhproto.LittleHorseClient = fake
			globalClient, globalConfig = &client, &littlehorse.LHConfig{}
			var prompt bytes.Buffer
			putTenantCmd.SetIn(strings.NewReader("yes\n"))
			putTenantCmd.SetErr(&prompt)
			defer putTenantCmd.SetIn(nil)
			defer putTenantCmd.SetErr(nil)
			err := runPutTenant(t, append([]string{"acme"}, tc.flags...)...)
			if tc.invalid {
				if err == nil || len(fake.calls) != 0 || prompt.Len() != 0 {
					t.Fatalf("expected rejection before prompt or RPC: %v, %v, %s", err, fake.calls, &prompt)
				}
				return
			}
			want := &lhproto.PutTenantRequest{Id: "acme", Name: &tc.displayName, OutputTopicConfig: tc.level}
			if err != nil || !proto.Equal(want, fake.request) || len(fake.calls) != 1 || fake.calls[0] != "put" {
				t.Fatalf("got %v, error %v, calls %v; want %v", fake.request, err, fake.calls, want)
			}
			for _, text := range []string{"acme", tc.displayName, "\n  \x1b[1mTenant ID:\x1b[0m", "\n  \x1b[1mDisplay Name:\x1b[0m", "\n  \x1b[1mOutput Topic Configuration:\x1b[0m", "overwrite", "[y/N]"} {
				if !strings.Contains(prompt.String(), text) {
					t.Fatalf("missing %q in prompt: %s", text, &prompt)
				}
			}
			if tc.level == nil && !strings.Contains(prompt.String(), "Output Topic Configuration:\x1b[0m Disabled") {
				t.Fatal("missing disable warning")
			}
		})
	}
}

func TestPutTenantConfirmation(t *testing.T) {
	previousClient, previousConfig := globalClient, globalConfig
	t.Cleanup(func() { globalClient, globalConfig = previousClient, previousConfig })
	for _, input := range []string{"y\n", "YES\n", " n\n", "\n", "maybe\n", "", "yes"} {
		t.Run(input, func(t *testing.T) {
			fake := &putTenantClient{}
			var client lhproto.LittleHorseClient = fake
			globalClient, globalConfig = &client, &littlehorse.LHConfig{}
			putTenantCmd.SetIn(strings.NewReader(input))
			var prompt bytes.Buffer
			putTenantCmd.SetErr(&prompt)
			defer putTenantCmd.SetIn(nil)
			defer putTenantCmd.SetErr(nil)
			err := runPutTenant(t, "acme")
			approved := input == "y\n" || input == "YES\n"
			if (err == nil) != approved || (fake.request != nil) != approved || fake.gets != 0 {
				t.Fatalf("input %q: error %v, calls %v", input, err, fake.calls)
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
			putTenantCmd.SetIn(strings.NewReader("yes\n"))
			defer putTenantCmd.SetIn(nil)
			if err := putTenantCmd.RunE(putTenantCmd, []string{"acme-prod"}); err != nil {
				t.Fatal(err)
			}
			searchTenantCmd.Run(searchTenantCmd, nil)
			if fake.putRequest.Name == nil || *fake.putRequest.Name != value {
				t.Fatal("put must always send the name, including empty")
			}
			for _, name := range []*string{fake.searchRequest.Name} {
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

func TestPutTenantDoesNotRetryFailedWrite(t *testing.T) {
	previousClient, previousConfig := globalClient, globalConfig
	t.Cleanup(func() { globalClient, globalConfig = previousClient, previousConfig })
	fake := &putTenantClient{putErr: status.Error(codes.Unavailable, "write failed")}
	var client lhproto.LittleHorseClient = fake
	globalClient, globalConfig = &client, &littlehorse.LHConfig{}
	putTenantCmd.SetIn(strings.NewReader("yes\n"))
	defer putTenantCmd.SetIn(nil)
	if err := runPutTenant(t, "acme", "--name=Acme"); status.Code(err) != codes.Unavailable {
		t.Fatalf("unexpected error: %v", err)
	}
	if len(fake.calls) != 1 || fake.calls[0] != "put" {
		t.Fatalf("unexpected calls: %v", fake.calls)
	}
}
