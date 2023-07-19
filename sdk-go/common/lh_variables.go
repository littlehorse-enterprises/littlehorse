package common

import (
	"encoding/base64"
	"encoding/json"
	"errors"
	"reflect"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

func StrToVarVal(input string, varType model.VariableTypePb) (*model.VariableValuePb, error) {
	out := &model.VariableValuePb{
		Type: varType,
	}

	var err error = nil

	switch varType {
	case model.VariableTypePb_BYTES:
		out.Bytes, err = base64.RawStdEncoding.DecodeString(input)

	case model.VariableTypePb_JSON_OBJ:
		jsonObj := make(map[string]interface{})
		// Just deserialize it to make sure it's legal
		err = json.Unmarshal([]byte(input), &jsonObj)
		if err == nil {
			out.JsonObj = &input
		}

	case model.VariableTypePb_JSON_ARR:
		jsonArr := make([]interface{}, 0)
		// Just deserialize it to make sure it's legal
		err = json.Unmarshal([]byte(input), &jsonArr)
		if err == nil {
			out.JsonArr = &input
		}

	case model.VariableTypePb_INT:
		// GoLang has this weird thing with scope of variables in switch...
		var tmp int64
		tmp, err = strconv.ParseInt(input, 10, 64)
		out.Int = &tmp

	case model.VariableTypePb_BOOL:
		var tmp bool
		tmp, err = strconv.ParseBool(input)
		out.Bool = &tmp

	case model.VariableTypePb_DOUBLE:
		var tmp float64
		tmp, err = strconv.ParseFloat(input, 64)
		out.Double = &tmp

	case model.VariableTypePb_STR:
		out.Str = &input

	case model.VariableTypePb_NULL:
		return nil, errors.New("creating void value not allowed here")
	}

	if err != nil {
		return nil, err
	}

	return out, nil
}

func GetVarType(thing interface{}) model.VariableTypePb {
	switch e := thing.(type) {
	case model.VariableTypePb:
		return e
	case int, int32, int64:
		return model.VariableTypePb_INT
	case float32, float64:
		return model.VariableTypePb_DOUBLE
	case string:
		return model.VariableTypePb_STR
	case bool:
		return model.VariableTypePb_BOOL
	case []byte:
		return model.VariableTypePb_BYTES
	default:
		// This is risky, for now we assume that all objects can be serialized
		// to JSON.
		isAList := reflect.TypeOf(e).Kind() == reflect.Slice
		if isAList {
			return model.VariableTypePb_JSON_ARR
		} else {
			return model.VariableTypePb_JSON_OBJ
		}
	}
}

func InterfaceToVarVal(someInterface interface{}) (*model.VariableValuePb, error) {
	out := &model.VariableValuePb{}
	var err error

	isPtr, _ := GetIsPtrAndType(reflect.TypeOf(someInterface))
	if someInterface == nil {
		return &model.VariableValuePb{
			Type: model.VariableTypePb_NULL,
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
		return &model.VariableValuePb{
			Type: model.VariableTypePb_NULL,
		}, nil
	}

	switch e := actualThing.(type) {
	case int:
		tmp := int64(e)
		out.Int = &tmp
		out.Type = model.VariableTypePb_INT
	case int16:
		tmp := int64(e)
		out.Int = &tmp
		out.Type = model.VariableTypePb_INT
	case int32:
		tmp := int64(e)
		out.Int = &tmp
		out.Type = model.VariableTypePb_INT
	case int64:
		tmp := int64(e)
		out.Int = &tmp
		out.Type = model.VariableTypePb_INT
	case float32:
		tmp := float64(e)
		out.Double = &tmp
		out.Type = model.VariableTypePb_DOUBLE
	case float64:
		tmp := float64(e)
		out.Double = &tmp
		out.Type = model.VariableTypePb_DOUBLE
	case string:
		out.Str = &e
		out.Type = model.VariableTypePb_STR
	case bool:
		out.Type = model.VariableTypePb_BOOL
		out.Bool = &e
	case []byte:
		out.Bytes = e
		out.Type = model.VariableTypePb_BYTES
	default:
		isAList := reflect.TypeOf(e).Kind() == reflect.Slice
		var b []byte
		b, err = json.Marshal(e)
		if err == nil {
			tmp := string(b)
			if isAList {
				out.JsonArr = &tmp
				out.Type = model.VariableTypePb_JSON_ARR
			} else {
				out.JsonObj = &tmp
				out.Type = model.VariableTypePb_JSON_OBJ
			}
		}
	}
	return out, err
}

func ReflectTypeToVarType(rt reflect.Type) model.VariableTypePb {
	switch rt.Kind() {
	case reflect.Ptr:
		return ReflectTypeToVarType(rt.Elem())
	case reflect.Int:
		return model.VariableTypePb_INT
	case reflect.Int16:
		return model.VariableTypePb_INT
	case reflect.Int32:
		return model.VariableTypePb_INT
	case reflect.Int64:
		return model.VariableTypePb_INT
	case reflect.Float32:
		return model.VariableTypePb_DOUBLE
	case reflect.Float64:
		return model.VariableTypePb_DOUBLE
	case reflect.Bool:
		return model.VariableTypePb_BOOL
	case reflect.String:
		return model.VariableTypePb_STR
	case reflect.Array:
		elemType := rt.Elem()
		switch elemType.Kind() {
		case reflect.Uint:
			return model.VariableTypePb_BYTES
		case reflect.Uint8:
			return model.VariableTypePb_BYTES
		default:
			return model.VariableTypePb_JSON_ARR
		}
	case reflect.Slice:
		elemType := rt.Elem()
		switch elemType.Kind() {
		case reflect.Uint:
			return model.VariableTypePb_BYTES
		case reflect.Uint8:
			return model.VariableTypePb_BYTES
		default:
			return model.VariableTypePb_JSON_ARR
		}
	default:
		return model.VariableTypePb_JSON_OBJ
	}
}
