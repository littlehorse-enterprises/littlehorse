package littlehorse

import "github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"

// LHInlineWorkflow builds a one-off workflow using the same thread API as a
// registered workflow. Compile returns a request for the RunInlineWf RPC.
type LHInlineWorkflow struct {
	workflow *LHWorkflow
	wfRunID  *string
}

// NewInlineWorkflow creates a workflow that does not need to be registered.
func NewInlineWorkflow(threadFunc ThreadFunc) *LHInlineWorkflow {
	return &LHInlineWorkflow{workflow: NewWorkflow(threadFunc, "inline")}
}

// WithWfRunID sets the ID of the resulting WfRun.
func (w *LHInlineWorkflow) WithWfRunID(id string) *LHInlineWorkflow {
	w.wfRunID = &id
	return w
}

// WithRetentionPolicy sets how long the resulting WfRun is retained after it terminates.
func (w *LHInlineWorkflow) WithRetentionPolicy(policy *lhproto.WorkflowRetentionPolicy) *LHInlineWorkflow {
	w.workflow.WithRetentionPolicy(policy)
	return w
}

// Compile builds a RunInlineWfRequest without making any server calls.
// Input variables can be set on the returned request before calling RunInlineWf.
func (w *LHInlineWorkflow) Compile() (*lhproto.RunInlineWfRequest, error) {
	compiled, err := w.workflow.Compile()
	if err != nil {
		return nil, err
	}
	return &lhproto.RunInlineWfRequest{
		WfSpec: &lhproto.InlineWfSpec{
			ThreadSpecs:          compiled.ThreadSpecs,
			EntrypointThreadName: compiled.EntrypointThreadName,
			RetentionPolicy:      compiled.RetentionPolicy,
		},
		Id: w.wfRunID,
	}, nil
}
