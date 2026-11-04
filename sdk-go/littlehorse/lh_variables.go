package littlehorse

import (
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"fmt"
	"reflect"
	"strconv"
	"strings"
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
	case lhproto.VariableType_TIMESTAMP:
		var ts *timestamppb.Timestamp
		ts, err = parseTimestamp(input)
		if err == nil {
			out.Value = &lhproto.VariableValue_UtcTimestamp{UtcTimestamp: ts}
		}
	}

	if err != nil {
		return nil, err
	}

	return out, nil
}

// parseTimestamp parses a TIMESTAMP from either epoch milliseconds or an
// ISO-8601 / RFC 3339 string.
func parseTimestamp(input string) (*timestamppb.Timestamp, error) {
	if ms, err := strconv.ParseInt(input, 10, 64); err == nil {
		return timestamppb.New(time.UnixMilli(ms)), nil
	}
	t, err := time.Parse(time.RFC3339, input)
	if err != nil {
		return nil, fmt.Errorf(
			"invalid TIMESTAMP %q: expected epoch milliseconds or an ISO-8601 string", input,
		)
	}
	return timestamppb.New(t), nil
}

// StructDefResolver fetches the StructDef metadata for a given StructDefId. It is
// used while coercing Struct-typed CLI input so that each field value can be
// interpreted against its declared field type. Implementations are expected to
// cache results, since a single input may reference the same StructDef many times
// (e.g. an Array<Struct> with many elements).
type StructDefResolver func(id *lhproto.StructDefId) (*lhproto.StructDef, error)

// TypeDefToVarVal converts a CLI input string into a VariableValue according to a
// registered TypeDefinition.
//
// This is equivalent to calling TypeDefToVarValWithResolver with a nil resolver,
// meaning Struct-typed input (via a StructDefId) is not supported.
func TypeDefToVarVal(input string, typeDef *lhproto.TypeDefinition) (*lhproto.VariableValue, error) {
	return TypeDefToVarValWithResolver(input, typeDef, nil)
}

// TypeDefToVarValWithResolver converts a CLI input string into a VariableValue
// according to a registered TypeDefinition.
//
// Primitive types keep the existing behavior (the input is a raw scalar string).
// Native Arrays (INLINE_ARRAY_DEF), Maps (INLINE_MAP_DEF) and Structs
// (STRUCT_DEF_ID / INLINE_STRUCT_DEF) are parsed as a JSON document and interpreted
// recursively against their declared types: JSON supplies the structure while the
// declared type supplies the meaning of each leaf.
//
// A non-nil resolver is required to coerce Struct values that are declared via a
// StructDefId, since the field schema lives in a separate StructDef that must be
// fetched from the server.
func TypeDefToVarValWithResolver(
	input string,
	typeDef *lhproto.TypeDefinition,
	resolver StructDefResolver,
) (*lhproto.VariableValue, error) {
	if typeDef == nil {
		return nil, fmt.Errorf("cannot convert value without a TypeDefinition")
	}

	switch typeDef.GetDefinedType().(type) {
	case *lhproto.TypeDefinition_InlineArrayDef,
		*lhproto.TypeDefinition_InlineMapDef,
		*lhproto.TypeDefinition_StructDefId,
		*lhproto.TypeDefinition_InlineStructDef:
		decoder := json.NewDecoder(strings.NewReader(input))
		// Preserve INT64 precision by decoding numbers as json.Number instead of float64.
		decoder.UseNumber()
		var node interface{}
		if err := decoder.Decode(&node); err != nil {
			return nil, fmt.Errorf("failed parsing JSON for Array/Map/Struct input: %w", err)
		}
		return jsonNodeToVarVal(node, typeDef, resolver)
	default:
		// PRIMITIVE_TYPE (or unset): interpret the input as a raw scalar string.
		return StrToVarVal(input, typeDef.GetPrimitiveType())
	}
}

// jsonNodeToVarVal recursively converts a decoded JSON node into a VariableValue,
// interpreting each node against the provided TypeDefinition.
func jsonNodeToVarVal(
	node interface{},
	typeDef *lhproto.TypeDefinition,
	resolver StructDefResolver,
) (*lhproto.VariableValue, error) {
	if node == nil {
		// A JSON null resolves to an unset VariableValue (LittleHorse NULL).
		return &lhproto.VariableValue{}, nil
	}

	switch dt := typeDef.GetDefinedType().(type) {
	case *lhproto.TypeDefinition_InlineArrayDef:
		items, ok := node.([]interface{})
		if !ok {
			return nil, fmt.Errorf("expected a JSON array for Array type, but got %T", node)
		}
		elementType := dt.InlineArrayDef.GetArrayType()
		out := &lhproto.Array{}
		for _, item := range items {
			itemVal, err := jsonNodeToVarVal(item, elementType, resolver)
			if err != nil {
				return nil, err
			}
			out.Items = append(out.Items, itemVal)
		}
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_Array{Array: out}}, nil

	case *lhproto.TypeDefinition_InlineMapDef:
		obj, ok := node.(map[string]interface{})
		if !ok {
			return nil, fmt.Errorf("expected a JSON object for Map type, but got %T", node)
		}
		keyType := dt.InlineMapDef.GetKeyType()
		valueType := dt.InlineMapDef.GetValueType()
		if _, isPrimitive := keyType.GetDefinedType().(*lhproto.TypeDefinition_PrimitiveType); !isPrimitive {
			return nil, fmt.Errorf("Map key type must be a primitive type")
		}
		out := &lhproto.Map{}
		for key, value := range obj {
			// JSON object keys are always strings; coerce to the declared key type.
			keyVal, err := StrToVarVal(key, keyType.GetPrimitiveType())
			if err != nil {
				return nil, fmt.Errorf("failed converting map key %q: %w", key, err)
			}
			valueVal, err := jsonNodeToVarVal(value, valueType, resolver)
			if err != nil {
				return nil, err
			}
			out.Entries = append(out.Entries, &lhproto.Map_Entry{Key: keyVal, Value: valueVal})
		}
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_Map{Map: out}}, nil

	case *lhproto.TypeDefinition_PrimitiveType:
		return jsonLeafToVarVal(node, dt.PrimitiveType)

	case *lhproto.TypeDefinition_StructDefId:
		return structNodeToVarVal(node, dt.StructDefId, nil, resolver)

	case *lhproto.TypeDefinition_InlineStructDef:
		return structNodeToVarVal(node, nil, dt.InlineStructDef, resolver)

	default:
		return nil, fmt.Errorf("unsupported or unset TypeDefinition for value")
	}
}

// structNodeToVarVal converts a decoded JSON object into a Struct VariableValue.
//
// The struct schema is resolved from either an inline InlineStructDef (when the
// variable is typed via INLINE_STRUCT_DEF) or by fetching the StructDef from the
// server via the resolver (when typed via STRUCT_DEF_ID). Each provided field is
// coerced against its declared field type; unknown fields and missing required
// fields are rejected. Fields that are absent but declare a default are omitted so
// that the server applies the default.
func structNodeToVarVal(
	node interface{},
	structDefId *lhproto.StructDefId,
	inlineStructDef *lhproto.InlineStructDef,
	resolver StructDefResolver,
) (*lhproto.VariableValue, error) {
	obj, ok := node.(map[string]interface{})
	if !ok {
		return nil, fmt.Errorf("expected a JSON object for Struct type, but got %T", node)
	}

	// Resolve the field schema.
	if inlineStructDef == nil {
		if structDefId == nil {
			return nil, fmt.Errorf("cannot resolve Struct schema without a StructDefId")
		}
		if resolver == nil {
			return nil, fmt.Errorf(
				"Struct values are not supported for this command (no StructDef resolver available)",
			)
		}
		structDef, err := resolver(structDefId)
		if err != nil {
			return nil, fmt.Errorf(
				"failed fetching StructDef %s (version %d): %w",
				structDefId.GetName(), structDefId.GetVersion(), err,
			)
		}
		inlineStructDef = structDef.GetStructDef()
		if inlineStructDef == nil {
			return nil, fmt.Errorf(
				"StructDef %s (version %d) has no schema", structDefId.GetName(), structDefId.GetVersion(),
			)
		}
	}

	fieldDefs := inlineStructDef.GetFields()

	// Reject unknown fields for a clearer error than the server would produce.
	for key := range obj {
		if _, ok := fieldDefs[key]; !ok {
			return nil, fmt.Errorf("unknown field %q for Struct", key)
		}
	}

	fields := make(map[string]*lhproto.StructField)
	for fieldName, fieldDef := range fieldDefs {
		rawValue, present := obj[fieldName]
		if !present {
			// Absent: rely on the server-side default when one is declared.
			if fieldDef.DefaultValue != nil {
				continue
			}
			// Absent and no default: a nullable field may simply be omitted.
			if fieldDef.GetIsNullable() {
				continue
			}
			return nil, fmt.Errorf("missing required field %q for Struct", fieldName)
		}

		if rawValue == nil {
			// Explicit JSON null is only allowed for nullable fields.
			if !fieldDef.GetIsNullable() {
				return nil, fmt.Errorf("field %q is not nullable but was null", fieldName)
			}
			fields[fieldName] = &lhproto.StructField{Value: &lhproto.VariableValue{}}
			continue
		}

		fieldVal, err := jsonNodeToVarVal(rawValue, fieldDef.GetFieldType(), resolver)
		if err != nil {
			return nil, fmt.Errorf("failed converting field %q: %w", fieldName, err)
		}
		fields[fieldName] = &lhproto.StructField{Value: fieldVal}
	}

	return &lhproto.VariableValue{
		Value: &lhproto.VariableValue_Struct{
			Struct: &lhproto.Struct{
				StructDefId: structDefId,
				Struct:      &lhproto.InlineStruct{Fields: fields},
			},
		},
	}, nil
}

// jsonLeafToVarVal converts a scalar JSON node into a primitive VariableValue.
func jsonLeafToVarVal(node interface{}, primitiveType lhproto.VariableType) (*lhproto.VariableValue, error) {
	// JSON_OBJ / JSON_ARR leaves carry a nested JSON structure verbatim.
	if primitiveType == lhproto.VariableType_JSON_OBJ || primitiveType == lhproto.VariableType_JSON_ARR {
		raw, err := json.Marshal(node)
		if err != nil {
			return nil, fmt.Errorf("failed re-serializing nested JSON value: %w", err)
		}
		return StrToVarVal(string(raw), primitiveType)
	}

	scalar, err := jsonScalarToString(node)
	if err != nil {
		return nil, err
	}
	return StrToVarVal(scalar, primitiveType)
}

// jsonScalarToString renders a scalar JSON node as the string form that
// StrToVarVal expects.
func jsonScalarToString(node interface{}) (string, error) {
	switch v := node.(type) {
	case string:
		return v, nil
	case json.Number:
		return v.String(), nil
	case bool:
		return strconv.FormatBool(v), nil
	default:
		return "", fmt.Errorf("expected a scalar JSON value but got %T", node)
	}
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
		eType := reflect.TypeOf(e)
		isAList := eType.Kind() == reflect.Slice

		// Check if the struct implements LHStructDef() — if so, serialize as a Struct proto.
		if eType.Kind() == reflect.Struct {
			structDefName := getStructDefName(eType)
			if structDefName != "" {
				structProto, structErr := ToLhStruct(e)
				if structErr != nil {
					return nil, structErr
				}
				out.Value = &lhproto.VariableValue_Struct{Struct: structProto}
				return out, nil
			}
		}

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

	case *lhproto.VariableValue_Struct:
		return StructProtoToGoStruct(varVal.GetStruct(), targetType)

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
	case *lhproto.VariableValue_UtcTimestamp:
		res := varVal.GetUtcTimestamp().AsTime()
		if isPtr {
			return &res, nil
		}
		return res, nil

	case *lhproto.VariableValue_WfRunId:
		workflowRunId := varVal.GetWfRunId()
		if isPtr {
			return &workflowRunId, nil
		}
		return workflowRunId, nil
	}

	return nil, fmt.Errorf("unknown VariableValue type")
}

func ReflectTypeToVarType(rt reflect.Type) lhproto.VariableType {
	// Handle non-primitive types first
	if rt == reflect.TypeOf(time.Time{}) {
		return lhproto.VariableType_TIMESTAMP
	} else if rt == reflect.TypeOf(lhproto.WfRunId{}) {
		return lhproto.VariableType_WF_RUN_ID
	}

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

// ReflectTypeToTypeDef converts a Go reflect.Type to a TypeDefinition proto.
// Unlike ReflectTypeToVarType, this detects struct types that implement LHStructDef()
// and returns a TypeDefinition with StructDefId instead of falling back to JSON_OBJ.
func ReflectTypeToTypeDef(rt reflect.Type) *lhproto.TypeDefinition {
	// Unwrap pointer
	if rt.Kind() == reflect.Ptr {
		rt = rt.Elem()
	}

	// Check if it's a struct with LHStructDef
	if rt.Kind() == reflect.Struct {
		structDefName := getStructDefName(rt)
		if structDefName != "" {
			return &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_StructDefId{
					StructDefId: &lhproto.StructDefId{
						Name:    structDefName,
						Version: -1,
					},
				},
			}
		}
	}

	// Fall back to primitive type
	primType := ReflectTypeToVarType(rt)
	return &lhproto.TypeDefinition{
		DefinedType: &lhproto.TypeDefinition_PrimitiveType{
			PrimitiveType: primType,
		},
	}
}
