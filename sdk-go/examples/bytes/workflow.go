package bytes

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func Greet(byteInput []byte) int {
	return len(byteInput)
}

func MyWorkflow(wf *littlehorse.WorkflowThread) {
	wf.Execute("greet", []byte("hello little horse"))
}
