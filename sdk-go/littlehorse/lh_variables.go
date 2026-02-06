package littlehorse

import (
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"reflect"
	"strconv"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
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

// VarValToInterface converts a VariableValue to a Go interface{}.
// This is the inverse operation of InterfaceToVarVal.
func VarValToInterface(varVal *lhproto.VariableValue) (interface{}, error) {
	switch v := varVal.GetValue().(type) {
	case *lhproto.VariableValue_Int:
		return v.Int, nil
	case *lhproto.VariableValue_Double:
		return v.Double, nil
	case *lhproto.VariableValue_Bool:
		return v.Bool, nil
	case *lhproto.VariableValue_Str:
		return v.Str, nil
	case *lhproto.VariableValue_Bytes:
		return v.Bytes, nil
	case *lhproto.VariableValue_JsonArr:
		// For JSON arrays, we need to deserialize to a generic structure
		var arr interface{}
		err := json.Unmarshal([]byte(v.JsonArr), &arr)
		if err != nil {
			return nil, fmt.Errorf("failed to deserialize JSON array: %w", err)
		}
		return arr, nil
	case *lhproto.VariableValue_JsonObj:
		// For JSON objects, we need to deserialize to a generic structure
		var obj interface{}
		err := json.Unmarshal([]byte(v.JsonObj), &obj)
		if err != nil {
			return nil, fmt.Errorf("failed to deserialize JSON object: %w", err)
		}
		return obj, nil
	case nil:
		return nil, nil
	default:
		return nil, fmt.Errorf("unknown VariableValue type")
	}
}

// loadByteArr loads a byte array from a VariableValue, handling various numeric conversions
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
	return nil, fmt.Errorf(
		"task input variable was of type BYTES but task function needed %s",
		kind.Kind().String(),
	)
}

// loadJsonArr deserializes a JSON array from a VariableValue into a specific Go type
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

// loadJsonObj deserializes a JSON object from a VariableValue into a specific Go type
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

// VarValToType converts a VariableValue to a specific Go type.
// This is similar to Java's varValToObj method - it handles type conversion based on the target type.
func VarValToType(varVal *lhproto.VariableValue, targetType reflect.Type) (interface{}, error) {
	isPtr, reflectKind := GetIsPtrAndType(targetType)

	// Handle null values
	if varVal.GetValue() == nil {
		if !isPtr {
			return nil, fmt.Errorf("got a NULL assignment for a non-pointer variable")
		}
		return nil, nil
	}

	// Special handling for JSON types that need type-specific deserialization
	switch varVal.GetValue().(type) {
	case *lhproto.VariableValue_JsonArr:
		if !isPtr {
			panic("task accepts a slice as an input variable; only pointers to slice are supported")
		}
		return loadJsonArr(varVal, targetType)

	case *lhproto.VariableValue_JsonObj:
		if !isPtr {
			panic("task accepts a struct as an input variable; only pointers to struct are supported")
		}
		return loadJsonObj(varVal, targetType)

	case *lhproto.VariableValue_Bytes:
		return loadByteArr(varVal, targetType)
	}

	// For primitive types, get the base value and convert to the target type
	baseValue, err := VarValToInterface(varVal)
	if err != nil {
		return nil, err
	}

	// Convert the base value to the expected type
	switch varVal.GetValue().(type) {
	case *lhproto.VariableValue_Int:
		int64Val := baseValue.(int64)
		switch reflectKind {
		case reflect.Int:
			result := int(int64Val)
			if isPtr {
				return &result, nil
			}
			return result, nil
		case reflect.Int16:
			result := int16(int64Val)
			if isPtr {
				return &result, nil
			}
			return result, nil
		case reflect.Int32:
			result := int32(int64Val)
			if isPtr {
				return &result, nil
			}
			return result, nil
		case reflect.Int64:
			result := int64Val
			if isPtr {
				return &result, nil
			}
			return result, nil
		}
		return nil, fmt.Errorf(
			"task input variable was of type INT but task function needed %s",
			reflectKind.String(),
		)

	case *lhproto.VariableValue_Double:
		float64Val := baseValue.(float64)
		switch reflectKind {
		case reflect.Float32:
			result := float32(float64Val)
			if isPtr {
				return &result, nil
			}
			return result, nil
		case reflect.Float64:
			result := float64Val
			if isPtr {
				return &result, nil
			}
			return result, nil
		}
		return nil, fmt.Errorf(
			"task input variable was of type DOUBLE but task function needed %s",
			reflectKind.String(),
		)

	case *lhproto.VariableValue_Bool:
		if reflectKind != reflect.Bool {
			return nil, fmt.Errorf(
				"task input variable was of type BOOL but task function needed %s",
				reflectKind.String(),
			)
		}
		boolVal := baseValue.(bool)
		if isPtr {
			return &boolVal, nil
		}
		return boolVal, nil

	case *lhproto.VariableValue_Str:
		if reflectKind != reflect.String {
			return nil, fmt.Errorf(
				"task input variable was of type STR but task function needed %s",
				reflectKind.String(),
			)
		}
		strVal := baseValue.(string)
		if isPtr {
			return &strVal, nil
		}
		return strVal, nil
	}

	return nil, fmt.Errorf("unknown VariableValue type")
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
