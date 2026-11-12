package littlehorse

import (
	"fmt"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"google.golang.org/protobuf/proto"
)

// UserTaskSchema defines a user task whose output conforms to a registered StructDef.
// The referenced version is the minimum output contract. Each new UserTaskRun pins
// the latest available version of that StructDef when the run is created.
type UserTaskSchema struct {
	name              string
	resultStructDefID *lhproto.StructDefId
}

// NewUserTaskSchema creates a user task schema using the ID returned when registering
// a StructDef. Inline StructDefs and unresolved versions (such as -1) are not supported.
// Call Compile to validate the schema and build a PutUserTaskDefRequest.
func NewUserTaskSchema(resultStructDefID *lhproto.StructDefId, name string) *UserTaskSchema {
	schema := &UserTaskSchema{name: name}
	if resultStructDefID != nil {
		schema.resultStructDefID = proto.Clone(resultStructDefID).(*lhproto.StructDefId)
	}
	return schema
}

// Compile builds the registration request. Register the StructDef first, then pass
// this request to LittleHorseClient.PutUserTaskDef. Legacy Fields are left unset.
func (s *UserTaskSchema) Compile() (*lhproto.PutUserTaskDefRequest, error) {
	if s == nil || strings.TrimSpace(s.name) == "" {
		return nil, fmt.Errorf("UserTaskDef name must not be empty")
	}
	if s.resultStructDefID == nil || strings.TrimSpace(s.resultStructDefID.GetName()) == "" {
		return nil, fmt.Errorf("a registered result StructDefId is required")
	}
	if s.resultStructDefID.GetVersion() < 0 {
		return nil, fmt.Errorf("result StructDefId must have a resolved version")
	}
	return &lhproto.PutUserTaskDefRequest{
		Name:              s.name,
		ResultStructDefId: proto.Clone(s.resultStructDefID).(*lhproto.StructDefId),
	}, nil
}
