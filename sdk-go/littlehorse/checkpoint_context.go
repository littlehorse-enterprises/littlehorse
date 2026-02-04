package littlehorse

import "fmt"

// CheckpointContext contains runtime information for a checkpoint execution.
// It provides logging capabilities that are specific to the checkpoint being executed.
type CheckpointContext struct {
	logOutput string
}

// NewCheckpointContext creates a new CheckpointContext instance.
func NewCheckpointContext() *CheckpointContext {
	return &CheckpointContext{
		logOutput: "",
	}
}

// Log appends the string representation of the provided item to the checkpoint's log output.
// If the item is nil, "nil" is appended to the log.
func (cc *CheckpointContext) Log(item interface{}) {
	if item != nil {
		cc.logOutput += fmt.Sprint(item)
	} else {
		cc.logOutput += "nil"
	}
}

// GetLogOutput returns the accumulated log output for this checkpoint.
func (cc *CheckpointContext) GetLogOutput() string {
	return cc.logOutput
}
