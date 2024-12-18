package littlehorse

import "github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"

type LHTaskException struct {
	Name    string
	Message string
	Content *lhproto.VariableValue
}
