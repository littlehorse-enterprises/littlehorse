package bytes

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const WorkflowName string = "bytes-workflow"
const TaskDefName string = "to-bytes"

func ToBytesLength(byteInput []byte) int {
	return len(byteInput)
}

func MyWorkflow(wf *littlehorse.WorkflowThread) {
	wf.Execute(TaskDefName, []byte("hello little horse"))
}
