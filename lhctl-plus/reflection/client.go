package reflection

import (
	"context"
	"fmt"
	"sort"
	"strings"

	"github.com/jhump/protoreflect/desc"
	"github.com/jhump/protoreflect/grpcreflect"
	"google.golang.org/grpc"
	rpb "google.golang.org/grpc/reflection/grpc_reflection_v1"
)

// Client wraps gRPC server reflection to discover services and RPCs.
type Client struct {
	refClient *grpcreflect.Client
}

// RPCInfo holds metadata about a single RPC method.
type RPCInfo struct {
	ServiceName string
	MethodName  string
	FullName    string
	Category    RPCCategory
	Description string
	Input       *desc.MessageDescriptor
	Output      *desc.MessageDescriptor
	Method      *desc.MethodDescriptor
}

// RPCCategory classifies the type of RPC operation.
type RPCCategory int

const (
	CategoryGet RPCCategory = iota
	CategoryPut
	CategoryRun
	CategorySearch
	CategoryDelete
	CategoryList
	CategoryOther
)

func (c RPCCategory) String() string {
	switch c {
	case CategoryGet:
		return "Get"
	case CategoryPut:
		return "Put"
	case CategoryRun:
		return "Run"
	case CategorySearch:
		return "Search"
	case CategoryDelete:
		return "Delete"
	case CategoryList:
		return "List"
	default:
		return "Other"
	}
}

// internalRPCs are excluded from the TUI — these are for task workers, not humans.
var internalRPCs = map[string]bool{
	"PollTask":           true,
	"ReportTask":         true,
	"RegisterTaskWorker": true,
	"PutCheckpoint":      true,
	"PutWfSpec":          true,
}

// NewClient creates a reflection client from an existing gRPC connection.
func NewClient(conn *grpc.ClientConn) *Client {
	stub := rpb.NewServerReflectionClient(conn)
	refClient := grpcreflect.NewClientV1(context.Background(), stub)
	return &Client{refClient: refClient}
}

// ListServices returns all gRPC service names available on the server.
func (c *Client) ListServices() ([]string, error) {
	services, err := c.refClient.ListServices()
	if err != nil {
		return nil, fmt.Errorf("listing services: %w", err)
	}
	return services, nil
}

// ListRPCs returns all RPC methods for the given service, categorized
// and filtered to exclude internal RPCs.
func (c *Client) ListRPCs(serviceName string) ([]RPCInfo, error) {
	svcDesc, err := c.refClient.ResolveService(serviceName)
	if err != nil {
		return nil, fmt.Errorf("resolving service %s: %w", serviceName, err)
	}

	methods := svcDesc.GetMethods()
	rpcs := make([]RPCInfo, 0, len(methods))

	for _, m := range methods {
		name := m.GetName()
		if internalRPCs[name] {
			continue
		}

		info := RPCInfo{
			ServiceName: serviceName,
			MethodName:  name,
			FullName:    fmt.Sprintf("/%s/%s", serviceName, name),
			Category:    categorize(name),
			Description: extractComment(m),
			Input:       m.GetInputType(),
			Output:      m.GetOutputType(),
			Method:      m,
		}
		rpcs = append(rpcs, info)
	}

	sort.Slice(rpcs, func(i, j int) bool {
		if rpcs[i].Category != rpcs[j].Category {
			return rpcs[i].Category < rpcs[j].Category
		}
		return rpcs[i].MethodName < rpcs[j].MethodName
	})

	return rpcs, nil
}

// ResolveMessage resolves a message descriptor by its fully qualified name.
func (c *Client) ResolveMessage(fqn string) (*desc.MessageDescriptor, error) {
	return c.refClient.ResolveMessage(fqn)
}

func categorize(name string) RPCCategory {
	switch {
	case strings.HasPrefix(name, "Get") || name == "Whoami":
		return CategoryGet
	case strings.HasPrefix(name, "Put"):
		return CategoryPut
	case strings.HasPrefix(name, "Run") || name == "ScheduleWf":
		return CategoryRun
	case strings.HasPrefix(name, "Search"):
		return CategorySearch
	case strings.HasPrefix(name, "Delete"):
		return CategoryDelete
	case strings.HasPrefix(name, "List"):
		return CategoryList
	default:
		return CategoryOther
	}
}

func extractComment(m *desc.MethodDescriptor) string {
	info := m.GetSourceInfo()
	if info == nil {
		return ""
	}
	comment := info.GetLeadingComments()
	comment = strings.TrimSpace(comment)
	// Take just the first sentence for brevity.
	if idx := strings.Index(comment, "."); idx > 0 && idx < 120 {
		return comment[:idx+1]
	}
	if len(comment) > 120 {
		return comment[:120] + "…"
	}
	return comment
}
