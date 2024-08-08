package common

import (
	"encoding/base64"
	"encoding/json"
	"reflect"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func StrToVarVal(input string, varType model.VariableType) (*model.VariableValue, error) {
	out := &model.VariableValue{}

	var err error = nil

	switch varType {
	case model.VariableType_BYTES:
		var bytesResult []byte
		bytesResult, err = base64.RawStdEncoding.DecodeString(input)

		out.Value = &model.VariableValue_Bytes{
			Bytes: bytesResult,
		}

	case model.VariableType_JSON_OBJ:
		jsonObj := make(map[string]interface{})
		// Just deserialize it to make sure it's legal
		err = json.Unmarshal([]byte(input), &jsonObj)
		if err == nil {
			out.Value = &model.VariableValue_JsonObj{JsonObj: input}
		}

	case model.VariableType_JSON_ARR:
		jsonArr := make([]interface{}, 0)
		// Just deserialize it to make sure it's legal
		err = json.Unmarshal([]byte(input), &jsonArr)
		if err == nil {
			out.Value = &model.VariableValue_JsonArr{JsonArr: input}
		}

	case model.VariableType_INT:
		// GoLang has this weird thing with scope of variables in switch...
		var tmp int64
		tmp, err = strconv.ParseInt(input, 10, 64)
		if err == nil {
			out.Value = &model.VariableValue_Int{Int: tmp}
		}

	case model.VariableType_BOOL:
		var tmp bool
		tmp, err = strconv.ParseBool(input)
		if err == nil {
			out.Value = &model.VariableValue_Bool{Bool: tmp}
		}

	case model.VariableType_DOUBLE:
		var tmp float64
		tmp, err = strconv.ParseFloat(input, 64)
		if err == nil {
			out.Value = &model.VariableValue_Double{Double: tmp}
		}

	case model.VariableType_STR, model.VariableType_MASK:
		out.Value = &model.VariableValue_Str{
			Str: input,
		}
	}

	if err != nil {
		return nil, err
	}

	return out, nil
}

func VarValToVarType(varVal *model.VariableValue) *model.VariableType {
	if varVal.GetValue() == nil {
		return nil
	}
	switch varVal.GetValue().(type) {
	case *model.VariableValue_Bool:
		result := model.VariableType_BOOL
		return &result
	case *model.VariableValue_Bytes:
		result := model.VariableType_BYTES
		return &result
	case *model.VariableValue_Double:
		result := model.VariableType_DOUBLE
		return &result
	case *model.VariableValue_Int:
		result := model.VariableType_INT
		return &result
	case *model.VariableValue_JsonArr:
		result := model.VariableType_JSON_ARR
		return &result
	case *model.VariableValue_JsonObj:
		result := model.VariableType_JSON_OBJ
		return &result
	case *model.VariableValue_Str:
	}
	result := model.VariableType_STR
	return &result
}

func GetVarType(thing interface{}) *model.VariableType {
	if thing == nil {
		return nil
	}
	switch e := thing.(type) {
	case model.VariableType:
		return &e
	case model.VariableValue:
		return VarValToVarType(&e)
	case *model.VariableValue:
		return VarValToVarType(e)
	case int, int32, int64, *int, *int32, *int64:
		result := model.VariableType_INT
		return &result
	case float32, float64, *float32, *float64:
		result := model.VariableType_DOUBLE
		return &result
	case string, *string:
		result := model.VariableType_STR
		return &result
	case bool, *bool:
		result := model.VariableType_BOOL
		return &result
	case []byte, *[]byte:
		result := model.VariableType_BYTES
		return &result
	default:
		// This is risky, for now we assume that all objects can be serialized
		// to JSON.
		isAList := reflect.TypeOf(e).Kind() == reflect.Slice
		if isAList {
			result := model.VariableType_JSON_ARR
			return &result
		} else {
			result := model.VariableType_JSON_OBJ
			return &result
		}
	}
}

func InterfaceToVarVal(someInterface interface{}) (*model.VariableValue, error) {
	out := &model.VariableValue{}
	var err error

	isPtr, _ := GetIsPtrAndType(reflect.TypeOf(someInterface))
	if someInterface == nil {
		return &model.VariableValue{}, nil
	}

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
		return &model.VariableValue{}, nil
	}

	switch e := actualThing.(type) {
	case int:
		tmp := int64(e)
		out.Value = &model.VariableValue_Int{Int: tmp}
	case int16:
		tmp := int64(e)
		out.Value = &model.VariableValue_Int{Int: tmp}
	case int32:
		tmp := int64(e)
		out.Value = &model.VariableValue_Int{Int: tmp}
	case int64:
		tmp := int64(e)
		out.Value = &model.VariableValue_Int{Int: tmp}
	case float32:
		tmp := float64(e)
		out.Value = &model.VariableValue_Double{Double: tmp}
	case float64:
		tmp := float64(e)
		out.Value = &model.VariableValue_Double{Double: tmp}
	case string:
		out.Value = &model.VariableValue_Str{Str: e}
	case bool:
		out.Value = &model.VariableValue_Bool{Bool: e}
	case []byte:
		out.Value = &model.VariableValue_Bytes{Bytes: e}
	default:
		isAList := reflect.TypeOf(e).Kind() == reflect.Slice
		var b []byte
		b, err = json.Marshal(e)
		if err == nil {
			tmp := string(b)
			if isAList {
				out.Value = &model.VariableValue_JsonArr{JsonArr: tmp}
			} else {
				out.Value = &model.VariableValue_JsonObj{JsonObj: tmp}
			}
		}
	}
	return out, err
}

func ReflectTypeToVarType(rt reflect.Type) model.VariableType {
	switch rt.Kind() {
	case reflect.Ptr:
		return ReflectTypeToVarType(rt.Elem())
	case reflect.Int:
		return model.VariableType_INT
	case reflect.Int16:
		return model.VariableType_INT
	case reflect.Int32:
		return model.VariableType_INT
	case reflect.Int64:
		return model.VariableType_INT
	case reflect.Float32:
		return model.VariableType_DOUBLE
	case reflect.Float64:
		return model.VariableType_DOUBLE
	case reflect.Bool:
		return model.VariableType_BOOL
	case reflect.String:
		return model.VariableType_STR
	case reflect.Array:
		elemType := rt.Elem()
		switch elemType.Kind() {
		case reflect.Uint:
			return model.VariableType_BYTES
		case reflect.Uint8:
			return model.VariableType_BYTES
		default:
			return model.VariableType_JSON_ARR
		}
	case reflect.Slice:
		elemType := rt.Elem()
		switch elemType.Kind() {
		case reflect.Uint:
			return model.VariableType_BYTES
		case reflect.Uint8:
			return model.VariableType_BYTES
		default:
			return model.VariableType_JSON_ARR
		}
	default:
		return model.VariableType_JSON_OBJ
	}
}
