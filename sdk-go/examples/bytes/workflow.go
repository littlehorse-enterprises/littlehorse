package bytes

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
)

func Greet(byteInput []byte) int {
	return len(byteInput)
}

func MyWorkflow(thread *wflib.ThreadBuilder) {
	thread.Execute("greet", []byte("hello little horse"))
}
