package littlehorse

import (
	"errors"
	"reflect"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

type TaskFuncArg struct {
	Name     string
	Type     reflect.Type
	Position int32
}

type TaskFuncSignature struct {
	Args                  []TaskFuncArg
	hasWorkerContextAtEnd bool
	HasOutput             bool
	HasError              bool
	OutputType            *reflect.Type
}

func (ts *TaskFuncSignature) GetHasWorkerContextAtEnd() bool {
	return ts.hasWorkerContextAtEnd
}

func NewTaskSignature(taskFunc interface{}) (*TaskFuncSignature, error) {
	fnType := reflect.TypeOf(taskFunc)

	if fnType.Kind() != reflect.Func {
		return nil, errors.New("must provide valid function")
	}

	out := &TaskFuncSignature{}

	for i := 0; i < fnType.NumIn(); i++ {
		argType := fnType.In(i)
		argName := fnType.In(i).Name()
		if argType.Kind() == reflect.Ptr && argType.Elem().Name() == "WorkerContext" {
			if i+1 != fnType.NumIn() {
				return nil, errors.New(
					"can only have worker context as the last parameter",
				)
			} else {
				out.hasWorkerContextAtEnd = true
				continue
			}
		} else if argType.Name() == "WorkerContext" {
			return nil, errors.New("worker context parameter must be a pointer")
		} else {
			out.Args = append(out.Args, TaskFuncArg{
				Name:     argName,
				Type:     argType,
				Position: int32(i),
			})
		}
	}

	numOut := fnType.NumOut()
	if numOut > 2 {
		return nil, errors.New(
			"task function can at most have one output return value and one error",
		)
	}

	if numOut == 0 {
	} else if numOut == 1 {
		if fnType.Out(0) == reflect.TypeOf((*error)(nil)).Elem() {
			out.HasError = true
		} else {
			funcReturn := fnType.Out(0)
			out.HasOutput = true
			out.OutputType = &funcReturn
		}
	} else {
		firstReturn := fnType.Out(0)
		secondReturn := fnType.Out(1)
		if firstReturn == reflect.TypeOf((*error)(nil)).Elem() {
			return nil, errors.New("first return must not be error")
		}

		if secondReturn != reflect.TypeOf((*error)(nil)).Elem() {
			return nil, errors.New("second return must be error")
		}

		out.HasError = true
		out.HasOutput = true
		out.OutputType = &firstReturn
	}
	return out, nil
}

func (a *TaskFuncArg) Assign(task *lhproto.ScheduledTask, context *WorkerContext) (*reflect.Value, error) {
	result, err := a.assign(task, context)
	if err != nil {
		return nil, err
	}
	temp := reflect.ValueOf(result)
	return &temp, nil
}

func (a *TaskFuncArg) assign(task *lhproto.ScheduledTask, context *WorkerContext) (interface{}, error) {
	if a.Type.Kind() == reflect.Ptr && a.Type.Elem().Name() == "WorkerContext" {
		return context, nil
	}

	varAssignment := task.Variables[a.Position]
	varVal := varAssignment.Value

	return VarValToType(varVal, a.Type)
}

func (a *TaskFuncArg) getType() (bool, reflect.Kind) {
	return GetIsPtrAndType(a.Type)
}
