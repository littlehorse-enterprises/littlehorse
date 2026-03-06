package littlehorse_test

import (
	"errors"
	"log"
	"reflect"
	"testing"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/stretchr/testify/assert"
)

func Task(name string) string {
	return ""
}

func TaskWithTimestamp(t time.Time) time.Time {
	return time.Now()
}

func TaskWithWfRunId(id lhproto.WfRunId) lhproto.WfRunId {
	return id
}

func TaskUsesWorkerContext(name string, wc *littlehorse.WorkerContext) string {
	return ""
}

func TaskUsesNonPointerWorkerContext(name string, wc littlehorse.WorkerContext) string {
	return ""
}

func TestNewTaskSignature(t *testing.T) {
	taskSig, err := littlehorse.NewTaskSignature(Task)

	assert.Nil(t, err)
	assert.Equal(t, littlehorse.TaskFuncArg{Name: "string", Type: reflect.TypeOf(""), Position: 0}, taskSig.Args[0])
	assert.Equal(t, reflect.TypeOf(""), *taskSig.OutputType)
}

func TestNewTaskSignatureWithTimestamp(t *testing.T) {
	taskSig, err := littlehorse.NewTaskSignature(TaskWithTimestamp)

	assert.Nil(t, err)
	assert.Equal(t, littlehorse.TaskFuncArg{Name: "Time", Type: reflect.TypeOf(time.Time{}), Position: 0}, taskSig.Args[0])
	assert.Equal(t, reflect.TypeOf(time.Time{}), *taskSig.OutputType)
}

func TestNewTaskSignatureWithWfRunId(t *testing.T) {
	taskSig, err := littlehorse.NewTaskSignature(TaskWithWfRunId)

	assert.Nil(t, err)
	assert.Equal(t, littlehorse.TaskFuncArg{Name: "WfRunId", Type: reflect.TypeOf(lhproto.WfRunId{}), Position: 0}, taskSig.Args[0])
	assert.Equal(t, reflect.TypeOf(lhproto.WfRunId{}), *taskSig.OutputType)
}

func TestNewTaskSignatureWithWorkerContext(t *testing.T) {
	taskSig, err := littlehorse.NewTaskSignature(TaskUsesWorkerContext)

	assert.Nil(t, err)
	assert.Equal(t, len(taskSig.Args), 1)
	assert.Equal(t, littlehorse.TaskFuncArg{Name: "string", Type: reflect.TypeOf(""), Position: 0}, taskSig.Args[0])
	assert.Equal(t, reflect.TypeOf(""), *taskSig.OutputType)
}

func TestTaskSigThrowsErrorWithNonPointerWorkerContext(t *testing.T) {
	_, err := littlehorse.NewTaskSignature(TaskUsesNonPointerWorkerContext)

	assert.Error(t, err)
	assert.EqualError(t, err, errors.New("worker context parameter must be a pointer").Error())
}

// Logs the Task Signature, useful for debugging
func LogTaskSig(taskSig *littlehorse.TaskFuncSignature) {
	for _, arg := range taskSig.Args {
		log.Printf("Name: %s", arg.Name)
		log.Printf("Type: %s", arg.Type.Name())
		log.Printf("Type.PkgPath: %s", arg.Type.PkgPath())
		log.Printf("Position: %d", arg.Position)
	}
}
