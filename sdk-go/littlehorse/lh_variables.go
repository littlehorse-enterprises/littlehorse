package littlehorse

import (
	"encoding/base64"
	"encoding/json"
	"reflect"
	"strconv"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func StrToVarVal(input string, varType lhproto.VariableType) (*lhproto.VariableValue, error) {
	out := &lhproto.VariableValue{}

	var err error = nil

	switch varType {
	case lhproto.VariableType_BYTES:
		var bytesResult []byte
		bytesResult, err = base64.RawStdEncoding.DecodeString(input)

		out.Value = &lhproto.VariableValue_Bytes{
			Bytes: bytesResult,
		}

	case lhproto.VariableType_JSON_OBJ:
		jsonObj := make(map[string]interface{})
		// Just deserialize it to make sure it's legal
		err = json.Unmarshal([]byte(input), &jsonObj)
		if err == nil {
			out.Value = &lhproto.VariableValue_JsonObj{JsonObj: input}
		}

	case lhproto.VariableType_JSON_ARR:
		jsonArr := make([]interface{}, 0)
		// Just deserialize it to make sure it's legal
		err = json.Unmarshal([]byte(input), &jsonArr)
		if err == nil {
			out.Value = &lhproto.VariableValue_JsonArr{JsonArr: input}
		}

	case lhproto.VariableType_INT:
		// GoLang has this weird thing with scope of variables in switch...
		var tmp int64
		tmp, err = strconv.ParseInt(input, 10, 64)
		if err == nil {
			out.Value = &lhproto.VariableValue_Int{Int: tmp}
		}

	case lhproto.VariableType_BOOL:
		var tmp bool
		tmp, err = strconv.ParseBool(input)
		if err == nil {
			out.Value = &lhproto.VariableValue_Bool{Bool: tmp}
		}

	case lhproto.VariableType_DOUBLE:
		var tmp float64
		tmp, err = strconv.ParseFloat(input, 64)
		if err == nil {
			out.Value = &lhproto.VariableValue_Double{Double: tmp}
		}

	case lhproto.VariableType_STR:
		out.Value = &lhproto.VariableValue_Str{
			Str: input,
		}
	case lhproto.VariableType_WF_RUN_ID:
		out.Value = &lhproto.VariableValue_WfRunId{
			WfRunId: StrToWfRunId(input),
		}
	}

	if err != nil {
		return nil, err
	}

	return out, nil
}

func VarValToVarType(varVal *lhproto.VariableValue) *lhproto.VariableType {
	if varVal.GetValue() == nil {
		return nil
	}
	switch varVal.GetValue().(type) {
	case *lhproto.VariableValue_Bool:
		result := lhproto.VariableType_BOOL
		return &result
	case *lhproto.VariableValue_Bytes:
		result := lhproto.VariableType_BYTES
		return &result
	case *lhproto.VariableValue_Double:
		result := lhproto.VariableType_DOUBLE
		return &result
	case *lhproto.VariableValue_Int:
		result := lhproto.VariableType_INT
		return &result
	case *lhproto.VariableValue_UtcTimestamp:
		result := lhproto.VariableType_TIMESTAMP
		return &result
	case *lhproto.VariableValue_WfRunId:
		result := lhproto.VariableType_WF_RUN_ID
		return &result
	case *lhproto.VariableValue_JsonArr:
		result := lhproto.VariableType_JSON_ARR
		return &result
	case *lhproto.VariableValue_JsonObj:
		result := lhproto.VariableType_JSON_OBJ
		return &result
	case *lhproto.VariableValue_Str:
	}
	result := lhproto.VariableType_STR
	return &result
}

func GetVarType(thing interface{}) *lhproto.VariableType {
	if thing == nil {
		return nil
	}
	switch e := thing.(type) {
	case lhproto.VariableType:
		return &e
	case lhproto.VariableValue:
		return VarValToVarType(&e)
	case *lhproto.VariableValue:
		return VarValToVarType(e)
	case int, int32, int64, *int, *int32, *int64:
		result := lhproto.VariableType_INT
		return &result
	case float32, float64, *float32, *float64:
		result := lhproto.VariableType_DOUBLE
		return &result
	case string, *string:
		result := lhproto.VariableType_STR
		return &result
	case bool, *bool:
		result := lhproto.VariableType_BOOL
		return &result
	case []byte, *[]byte:
		result := lhproto.VariableType_BYTES
		return &result
	default:
		// This is risky, for now we assume that all objects can be serialized
		// to JSON.
		isAList := reflect.TypeOf(e).Kind() == reflect.Slice
		if isAList {
			result := lhproto.VariableType_JSON_ARR
			return &result
		} else {
			result := lhproto.VariableType_JSON_OBJ
			return &result
		}
	}
}

func InterfaceToVarVal(someInterface interface{}) (*lhproto.VariableValue, error) {
	if someInterface == nil {
		return &lhproto.VariableValue{}, nil
	}

	out := &lhproto.VariableValue{}
	var err error

	interfaceAsVarVal, isVarVal := someInterface.(*lhproto.VariableValue)
	if isVarVal {
		return interfaceAsVarVal, nil
	}

	isPtr, _ := GetIsPtrAndType(reflect.TypeOf(someInterface))

	var actualThing interface{}
	if isPtr {
		actualThingReflect := reflect.ValueOf(someInterface)
		if actualThingReflect.IsNil() {
			actualThing = nil
		} else {
			actualThing = actualThingReflect.Elem().Interface()
		}
	} else {
		actualThing = someInterface
	}

	if actualThing == nil {
		return &lhproto.VariableValue{}, nil
	}

	switch e := actualThing.(type) {
	case int:
		tmp := int64(e)
		out.Value = &lhproto.VariableValue_Int{Int: tmp}
	case int16:
		tmp := int64(e)
		out.Value = &lhproto.VariableValue_Int{Int: tmp}
	case int32:
		tmp := int64(e)
		out.Value = &lhproto.VariableValue_Int{Int: tmp}
	case int64:
		tmp := int64(e)
		out.Value = &lhproto.VariableValue_Int{Int: tmp}
	case float32:
		tmp := float64(e)
		out.Value = &lhproto.VariableValue_Double{Double: tmp}
	case float64:
		tmp := float64(e)
		out.Value = &lhproto.VariableValue_Double{Double: tmp}
	case string:
		out.Value = &lhproto.VariableValue_Str{Str: e}
	case bool:
		out.Value = &lhproto.VariableValue_Bool{Bool: e}
	case time.Time:
		out.Value = &lhproto.VariableValue_UtcTimestamp{UtcTimestamp: timestamppb.New(e.UTC())}
	case lhproto.WfRunId:
		out.Value = &lhproto.VariableValue_WfRunId{WfRunId: &e}
	case []byte:
		out.Value = &lhproto.VariableValue_Bytes{Bytes: e}
	default:
		isAList := reflect.TypeOf(e).Kind() == reflect.Slice
		var b []byte
		b, err = json.Marshal(e)
		if err == nil {
			tmp := string(b)
			if isAList {
				out.Value = &lhproto.VariableValue_JsonArr{JsonArr: tmp}
			} else {
				out.Value = &lhproto.VariableValue_JsonObj{JsonObj: tmp}
			}
		}
	}
	return out, err
}

func ReflectTypeToVarType(rt reflect.Type) lhproto.VariableType {
	switch rt.Kind() {
	case reflect.Ptr:
		return ReflectTypeToVarType(rt.Elem())
	case reflect.Int:
		return lhproto.VariableType_INT
	case reflect.Int16:
		return lhproto.VariableType_INT
	case reflect.Int32:
		return lhproto.VariableType_INT
	case reflect.Int64:
		return lhproto.VariableType_INT
	case reflect.Float32:
		return lhproto.VariableType_DOUBLE
	case reflect.Float64:
		return lhproto.VariableType_DOUBLE
	case reflect.Bool:
		return lhproto.VariableType_BOOL
	case reflect.String:
		return lhproto.VariableType_STR
	case reflect.Array:
		elemType := rt.Elem()
		switch elemType.Kind() {
		case reflect.Uint:
			return lhproto.VariableType_BYTES
		case reflect.Uint8:
			return lhproto.VariableType_BYTES
		default:
			return lhproto.VariableType_JSON_ARR
		}
	case reflect.Slice:
		elemType := rt.Elem()
		switch elemType.Kind() {
		case reflect.Uint:
			return lhproto.VariableType_BYTES
		case reflect.Uint8:
			return lhproto.VariableType_BYTES
		default:
			return lhproto.VariableType_JSON_ARR
		}
	default:
		return lhproto.VariableType_JSON_OBJ
	}
}
