package littlehorse

import (
	"context"
	"testing"
	"testing/synctest"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type registrationRetryClient struct {
	lhproto.LittleHorseClient
	manager  *serverConnectionManager
	failures []bool
	attempts []time.Time
}

func (c *registrationRetryClient) GetTaskDef(context.Context, *lhproto.TaskDefId, ...grpc.CallOption) (*lhproto.TaskDef, error) {
	return &lhproto.TaskDef{}, nil
}

func (c *registrationRetryClient) RegisterTaskWorker(context.Context, *lhproto.RegisterTaskWorkerRequest, ...grpc.CallOption) (*lhproto.RegisterTaskWorkerResponse, error) {
	index := len(c.attempts)
	c.attempts = append(c.attempts, time.Now())
	if index == len(c.failures)-1 {
		c.manager.running = false
	}
	if c.failures[index] {
		return nil, status.Error(codes.Unavailable, "unavailable")
	}
	return &lhproto.RegisterTaskWorkerResponse{}, nil
}

func TestInitialWorkerRegistrationRetries(t *testing.T) {
	for _, test := range []struct {
		name     string
		failures []bool
		delays   []time.Duration
	}{
		{"initial failures", []bool{true, true, false, true, false}, []time.Duration{time.Second, time.Second, time.Minute, 8 * time.Second}},
		{"initial success", []bool{false, true, false}, []time.Duration{time.Minute, 8 * time.Second}},
	} {
		t.Run(test.name, func(t *testing.T) {
			synctest.Test(t, func(t *testing.T) {
				client := &registrationRetryClient{failures: test.failures}
				var stub lhproto.LittleHorseClient = client
				worker := &LHTaskWorker{
					config:    &LHConfig{TaskWorkerId: "retry-worker"},
					grpcStub:  &stub,
					taskDefId: &lhproto.TaskDefId{Name: "retry-task"},
				}
				client.manager = newServerConnectionManager(worker)
				if err := client.manager.start(); err != nil {
					t.Fatal(err)
				}
				for i, expected := range test.delays {
					if actual := client.attempts[i+1].Sub(client.attempts[i]); actual != expected {
						t.Errorf("retry %d: got %v, want %v", i+1, actual, expected)
					}
				}
			})
		})
	}
}
