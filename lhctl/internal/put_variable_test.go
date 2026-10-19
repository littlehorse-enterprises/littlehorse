package internal

import (
	"context"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"google.golang.org/grpc"
)

type putVariableClient struct {
	lhproto.LittleHorseClient
	request *lhproto.PutVariableRequest
}

func (c *putVariableClient) PutVariable(_ context.Context, req *lhproto.PutVariableRequest, _ ...grpc.CallOption) (*lhproto.VariableValue, error) {
	c.request = req
	return &lhproto.VariableValue{Value: &lhproto.VariableValue_Int{Int: 7}}, nil
}

func TestPutVariableCommand(t *testing.T) {
	previousClient, previousConfig := globalClient, globalConfig
	t.Cleanup(func() { globalClient, globalConfig = previousClient, previousConfig })
	fake := &putVariableClient{}
	var client lhproto.LittleHorseClient = fake
	globalClient = &client
	globalConfig = &littlehorse.LHConfig{}
	args := []string{"parent_child", "2", "counter", "INT", "42"}
	if err := putVariableCmd.ValidateArgs(args); err != nil {
		t.Fatal(err)
	}
	if err := putVariableCmd.RunE(putVariableCmd, args); err != nil {
		t.Fatal(err)
	}
	req := fake.request
	if req == nil || req.Id.WfRunId.Id != "child" || req.Id.WfRunId.ParentWfRunId.Id != "parent" ||
		req.Id.ThreadRunNumber != 2 || req.Id.Name != "counter" || req.Value.GetInt() != 42 {
		t.Fatalf("unexpected request: %v", req)
	}
}

func TestPutVariableNullAndEmptyString(t *testing.T) {
	for _, args := range [][]string{
		{"run", "0", "value"},
		{"run", "0", "value", "STR", ""},
	} {
		req, err := parsePutVariableRequest(args)
		if err != nil {
			t.Fatal(err)
		}
		if req.Value == nil || (req.Value.GetValue() == nil) != (len(args) == 3) {
			t.Fatalf("null and empty string must remain distinct: %v", req)
		}
	}
}

func TestPutVariableRejectsInvalidArguments(t *testing.T) {
	for _, args := range [][]string{
		{}, {"run", "0", "value", "INT"},
		{"run", "-1", "value"}, {"run", "2147483648", "value"},
		{"run", "no", "value"}, {"", "0", "value"}, {"run", "0", ""},
		{"run", "0", "value", "UNKNOWN", "1"},
		{"run", "0", "value", "INT", "no"},
		{"run", "0", "value", "JSON_OBJ", "{"},
	} {
		if err := putVariableCmd.ValidateArgs(args); err == nil {
			t.Errorf("expected invalid arguments: %v", args)
		}
	}
}
