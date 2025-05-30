package littlehorse

import (
	"encoding/binary"
	"encoding/json"
	"errors"
	"fmt"
	"reflect"
	"strings"

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

	isPtr, reflectType := a.getType()

	// switch varVal.Type {
	switch varVal.GetValue().(type) {
	case *lhproto.VariableValue_Int:
		switch reflectType {
		case reflect.Int:
			if isPtr {
				result := int(varVal.GetInt())
				return &result, nil
			} else {
				return int(varVal.GetInt()), nil
			}
		case reflect.Int16:
			if isPtr {
				result := int16(varVal.GetInt())
				return &result, nil
			} else {
				return int16(varVal.GetInt()), nil
			}
		case reflect.Int32:
			if isPtr {
				result := int32(varVal.GetInt())
				return &result, nil
			} else {
				return int32(varVal.GetInt()), nil
			}
		case reflect.Int64:
			if isPtr {
				result := int32(varVal.GetInt())
				return &result, nil
			} else {
				return int32(varVal.GetInt()), nil
			}
		}
		return nil, errors.New(
			"task input variable was of type INT but task function needed " + reflectType.String(),
		)

	case *lhproto.VariableValue_Double:
		switch a.Type.Kind() {
		case reflect.Float32:
			if isPtr {
				result := float32(varVal.GetDouble())
				return &result, nil
			} else {
				return float32(varVal.GetDouble()), nil
			}
		case reflect.Float64:
			if isPtr {
				result := float64(varVal.GetDouble())
				return &result, nil
			} else {
				return float64(varVal.GetDouble()), nil
			}
		}
		return nil, errors.New(
			"task input variable was of type DOUBLE but task function needed " + reflectType.String(),
		)

	case *lhproto.VariableValue_Bool:
		if reflectType != reflect.Bool {
			return nil, errors.New(
				"task input variable was of type BOOL but task function needed " + reflectType.String(),
			)
		}
		if isPtr {
			tmp := varVal.GetBool()
			return &tmp, nil
		} else {
			return varVal.GetBool(), nil
		}

	case *lhproto.VariableValue_Str:
		if reflectType != reflect.String {
			return nil, errors.New(
				"task input variable was of type STR but task function needed " + reflectType.String(),
			)
		}
		if isPtr {
			tmp := varVal.GetStr()
			return &tmp, nil
		} else {
			return varVal.GetStr(), nil
		}

	case *lhproto.VariableValue_Bytes:
		return loadByteArr(varVal, a.Type)

	case *lhproto.VariableValue_JsonArr:
		if !isPtr {
			panic("task accepts a slice as an input variable; only pointers to slice are supported")
		}
		return loadJsonArr(varVal, a.Type)

	case *lhproto.VariableValue_JsonObj:
		if !isPtr {
			panic("task accepts a struct as an input variable; only pointers to struct are supported")
		}
		return loadJsonObj(varVal, a.Type)

	case nil:
		if !isPtr {
			return nil, errors.New("got a NULL assignment for a non-pointer variable")
		}
		return nil, nil
	}

	PrintProto(varVal)

	return nil, errors.New("impossible: couldn't find type of VariableValue")
}

func (a *TaskFuncArg) getType() (bool, reflect.Kind) {
	return GetIsPtrAndType(a.Type)
}

// TODO
func loadByteArr(varVal *lhproto.VariableValue, kind reflect.Type) (interface{}, error) {
	switch kind.Kind() {
	case reflect.Slice, reflect.Array:
		return []byte(varVal.GetBytes()), nil
	case reflect.Uint:
		return uint(varVal.GetBytes()[8]), nil
	case reflect.Uint8:
		return uint8(varVal.GetBytes()[8]), nil
	case reflect.Uint16:
		return binary.BigEndian.Uint16(varVal.GetBytes()), nil
	case reflect.Uint32:
		return binary.BigEndian.Uint32(varVal.GetBytes()), nil
	case reflect.Uint64:
		return binary.BigEndian.Uint64(varVal.GetBytes()), nil
	}
	return nil, errors.New(
		"task input variable was of type BYTES but task function needed " + kind.Kind().String(),
	)
}

func loadJsonArr(varVal *lhproto.VariableValue, kind reflect.Type) (interface{}, error) {
	strPointerValue := varVal.GetJsonArr()
	decoder := json.NewDecoder(strings.NewReader(strPointerValue))

	obj := reflect.New(kind.Elem())
	objPtr := obj.Interface()

	err := decoder.Decode(objPtr)
	if err != nil {
		return nil, fmt.Errorf("Error Decoding the Json Array: %s", err.Error())
	}
	return objPtr, nil
}

func loadJsonObj(varVal *lhproto.VariableValue, kind reflect.Type) (interface{}, error) {
	strPointerValue := varVal.GetJsonObj()
	decoder := json.NewDecoder(strings.NewReader(strPointerValue))

	obj := reflect.New(kind.Elem())
	objPtr := obj.Interface()

	err := decoder.Decode(objPtr)
	if err != nil {
		return nil, fmt.Errorf("Error Decoding the Json Obj: %s", err.Error())
	}
	return objPtr, nil
}
