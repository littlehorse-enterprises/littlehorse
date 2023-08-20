package common

import (
	"encoding/base64"
	"encoding/json"
	"errors"
	"reflect"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func StrToVarVal(input string, varType model.VariableType) (*model.VariableValue, error) {
	out := &model.VariableValue{
		Type: varType,
	}

	var err error = nil

	switch varType {
	case model.VariableType_BYTES:
		out.Bytes, err = base64.RawStdEncoding.DecodeString(input)

	case model.VariableType_JSON_OBJ:
		jsonObj := make(map[string]interface{})
		// Just deserialize it to make sure it's legal
		err = json.Unmarshal([]byte(input), &jsonObj)
		if err == nil {
			out.JsonObj = &input
		}

	case model.VariableType_JSON_ARR:
		jsonArr := make([]interface{}, 0)
		// Just deserialize it to make sure it's legal
		err = json.Unmarshal([]byte(input), &jsonArr)
		if err == nil {
			out.JsonArr = &input
		}

	case model.VariableType_INT:
		// GoLang has this weird thing with scope of variables in switch...
		var tmp int64
		tmp, err = strconv.ParseInt(input, 10, 64)
		out.Int = &tmp

	case model.VariableType_BOOL:
		var tmp bool
		tmp, err = strconv.ParseBool(input)
		out.Bool = &tmp

	case model.VariableType_DOUBLE:
		var tmp float64
		tmp, err = strconv.ParseFloat(input, 64)
		out.Double = &tmp

	case model.VariableType_STR:
		out.Str = &input

	case model.VariableType_NULL:
		return nil, errors.New("creating void value not allowed here")
	}

	if err != nil {
		return nil, err
	}

	return out, nil
}

func GetVarType(thing interface{}) model.VariableType {
	switch e := thing.(type) {
	case model.VariableType:
		return e
	case int, int32, int64:
		return model.VariableType_INT
	case float32, float64:
		return model.VariableType_DOUBLE
	case string:
		return model.VariableType_STR
	case bool:
		return model.VariableType_BOOL
	case []byte:
		return model.VariableType_BYTES
	default:
		// This is risky, for now we assume that all objects can be serialized
		// to JSON.
		isAList := reflect.TypeOf(e).Kind() == reflect.Slice
		if isAList {
			return model.VariableType_JSON_ARR
		} else {
			return model.VariableType_JSON_OBJ
		}
	}
}

func InterfaceToVarVal(someInterface interface{}) (*model.VariableValue, error) {
	out := &model.VariableValue{}
	var err error

	isPtr, _ := GetIsPtrAndType(reflect.TypeOf(someInterface))
	if someInterface == nil {
		return &model.VariableValue{
			Type: model.VariableType_NULL,
		}, nil
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
		return &model.VariableValue{
			Type: model.VariableType_NULL,
		}, nil
	}

	switch e := actualThing.(type) {
	case int:
		tmp := int64(e)
		out.Int = &tmp
		out.Type = model.VariableType_INT
	case int16:
		tmp := int64(e)
		out.Int = &tmp
		out.Type = model.VariableType_INT
	case int32:
		tmp := int64(e)
		out.Int = &tmp
		out.Type = model.VariableType_INT
	case int64:
		tmp := int64(e)
		out.Int = &tmp
		out.Type = model.VariableType_INT
	case float32:
		tmp := float64(e)
		out.Double = &tmp
		out.Type = model.VariableType_DOUBLE
	case float64:
		tmp := float64(e)
		out.Double = &tmp
		out.Type = model.VariableType_DOUBLE
	case string:
		out.Str = &e
		out.Type = model.VariableType_STR
	case bool:
		out.Type = model.VariableType_BOOL
		out.Bool = &e
	case []byte:
		out.Bytes = e
		out.Type = model.VariableType_BYTES
	default:
		isAList := reflect.TypeOf(e).Kind() == reflect.Slice
		var b []byte
		b, err = json.Marshal(e)
		if err == nil {
			tmp := string(b)
			if isAList {
				out.JsonArr = &tmp
				out.Type = model.VariableType_JSON_ARR
			} else {
				out.JsonObj = &tmp
				out.Type = model.VariableType_JSON_OBJ
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
