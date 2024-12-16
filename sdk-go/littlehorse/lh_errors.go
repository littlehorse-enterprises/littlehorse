package littlehorse

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

// LHTaskException defines a custom error type.
type LHTaskException struct {
	Name    string
	Message string
	Content *lhproto.VariableValue // Pointer to VariableValue, can be nil
}
