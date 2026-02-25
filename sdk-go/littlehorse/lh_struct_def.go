package littlehorse

import (
	"context"
	"fmt"
	"reflect"
	"strings"
	"unicode"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

// GoStructToInlineStructDef uses reflection to convert a Go struct instance into an
// InlineStructDef protobuf. The struct's exported fields are converted to StructFieldDef entries.
//
// Field names are resolved in order: "lh" struct tag, then "json" struct tag, then
// PascalCase-to-camelCase conversion of the Go field name. Use lh:"-" to skip a field.
//
// Supported field types:
//   - string -> STR
//   - int, int16, int32, int64 -> INT
//   - float32, float64 -> DOUBLE
//   - bool -> BOOL
//   - []byte -> BYTES
//   - slices/arrays -> JSON_ARR
//   - structs with LHStructName() -> nested StructDef reference
func GoStructToInlineStructDef(structInstance interface{}) (*lhproto.InlineStructDef, error) {
	t := reflect.TypeOf(structInstance)
	if t.Kind() == reflect.Ptr {
		t = t.Elem()
	}
	if t.Kind() != reflect.Struct {
		return nil, fmt.Errorf("expected a struct type, got %s", t.Kind())
	}
	return buildInlineStructDef(t)
}

func buildInlineStructDef(t reflect.Type) (*lhproto.InlineStructDef, error) {
	fields := make(map[string]*lhproto.StructFieldDef)

	for i := 0; i < t.NumField(); i++ {
		field := t.Field(i)

		// Skip unexported fields.
		if !field.IsExported() {
			continue
		}

		// Check for lh struct tag.
		tag := field.Tag.Get("lh")
		if tag == "-" {
			continue
		}

		// Resolve field name: lh tag > json tag > PascalCase-to-camelCase.
		fieldName := tag
		if fieldName == "" {
			if jsonTag := field.Tag.Get("json"); jsonTag != "" {
				fieldName = strings.SplitN(jsonTag, ",", 2)[0]
			}
		}
		if fieldName == "" {
			fieldName = goFieldNameToCamelCase(field.Name)
		}

		fieldDef, err := goTypeToStructFieldDef(field.Type)
		if err != nil {
			return nil, fmt.Errorf("field %s: %w", field.Name, err)
		}

		fields[fieldName] = fieldDef
	}

	return &lhproto.InlineStructDef{
		Fields: fields,
	}, nil
}

func goTypeToStructFieldDef(t reflect.Type) (*lhproto.StructFieldDef, error) {
	isPtr := false
	if t.Kind() == reflect.Ptr {
		isPtr = true
		t = t.Elem()
	}

	// Check if it's a struct (nested StructDef).
	if t.Kind() == reflect.Struct {
		structDefName := getStructDefName(t)
		if structDefName == "" {
			return nil, fmt.Errorf(
				"nested struct type %s must implement LHStructName() method to be used as a StructDef field",
				t.Name(),
			)
		}
		return &lhproto.StructFieldDef{
			FieldType: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_StructDefId{
					StructDefId: &lhproto.StructDefId{
						Name: structDefName,
					},
				},
			},
		}, nil
	}

	varType, err := goKindToVariableType(t, isPtr)
	if err != nil {
		return nil, err
	}

	return &lhproto.StructFieldDef{
		FieldType: &lhproto.TypeDefinition{
			DefinedType: &lhproto.TypeDefinition_PrimitiveType{
				PrimitiveType: varType,
			},
		},
	}, nil
}

func goKindToVariableType(t reflect.Type, isPtr bool) (lhproto.VariableType, error) {
	kind := t.Kind()

	switch kind {
	case reflect.String:
		return lhproto.VariableType_STR, nil
	case reflect.Int, reflect.Int16, reflect.Int32, reflect.Int64:
		return lhproto.VariableType_INT, nil
	case reflect.Float32, reflect.Float64:
		return lhproto.VariableType_DOUBLE, nil
	case reflect.Bool:
		return lhproto.VariableType_BOOL, nil
	case reflect.Slice:
		if t.Elem().Kind() == reflect.Uint8 {
			return lhproto.VariableType_BYTES, nil
		}
		return lhproto.VariableType_JSON_ARR, nil
	case reflect.Array:
		if t.Elem().Kind() == reflect.Uint8 {
			return lhproto.VariableType_BYTES, nil
		}
		return lhproto.VariableType_JSON_ARR, nil
	default:
		return 0, fmt.Errorf("unsupported Go type %s for StructDef field", t.Kind())
	}
}

// goFieldNameToCamelCase converts a Go PascalCase field name to camelCase.
// Examples: "FirstName" -> "firstName", "ZipCode" -> "zipCode", "HTTPServer" -> "httpServer".
func goFieldNameToCamelCase(name string) string {
	if name == "" {
		return name
	}

	runes := []rune(name)
	result := make([]rune, 0, len(runes))

	// Find the end of the leading uppercase sequence.
	i := 0
	for i < len(runes) && unicode.IsUpper(runes[i]) {
		i++
	}

	if i == 0 {
		// Already starts with lowercase.
		return name
	}

	if i == 1 {
		// Single leading uppercase letter — just lowercase it.
		result = append(result, unicode.ToLower(runes[0]))
		result = append(result, runes[1:]...)
	} else if i == len(runes) {
		// All uppercase — lowercase all.
		for _, r := range runes {
			result = append(result, unicode.ToLower(r))
		}
	} else {
		// Multiple leading uppercase letters (e.g., "HTTPServer" -> "httpServer").
		// Lowercase all but the last uppercase letter, which starts the next word.
		for j := 0; j < i-1; j++ {
			result = append(result, unicode.ToLower(runes[j]))
		}
		result = append(result, runes[i-1:]...)
	}

	return string(result)
}

// getStructDefName returns the StructDef name for a Go type if it implements
// LHStructName() string, or returns empty string otherwise.
func getStructDefName(t reflect.Type) string {
	// Check if the type (or pointer to it) has a LHStructName method.
	ptrType := reflect.PointerTo(t)
	method, ok := ptrType.MethodByName("LHStructName")
	if !ok {
		method, ok = t.MethodByName("LHStructName")
		if !ok {
			return ""
		}
		// Method on value receiver.
		if method.Type.NumOut() != 1 || method.Type.Out(0).Kind() != reflect.String {
			return ""
		}
		instance := reflect.New(t).Elem()
		results := method.Func.Call([]reflect.Value{instance})
		return results[0].String()
	}

	// Method on pointer receiver.
	if method.Type.NumOut() != 1 || method.Type.Out(0).Kind() != reflect.String {
		return ""
	}
	instance := reflect.New(t)
	results := method.Func.Call([]reflect.Value{instance})
	return results[0].String()
}

// GoStructToStructProto converts a Go struct instance into a Struct protobuf suitable for
// use as a VariableValue. The struct must implement LHStructName() string.
// This is the Go equivalent of Java's LHLibUtil.serializeToStruct() and .NET's SerializeToStruct().
func GoStructToStructProto(structInstance interface{}) (*lhproto.Struct, error) {
	v := reflect.ValueOf(structInstance)
	t := v.Type()
	if t.Kind() == reflect.Ptr {
		v = v.Elem()
		t = t.Elem()
	}
	if t.Kind() != reflect.Struct {
		return nil, fmt.Errorf("expected a struct type, got %s", t.Kind())
	}

	structDefName := getStructDefName(t)
	if structDefName == "" {
		return nil, fmt.Errorf("struct type %s must implement LHStructName() string", t.Name())
	}

	inlineStruct, err := goValueToInlineStruct(v)
	if err != nil {
		return nil, err
	}

	return &lhproto.Struct{
		StructDefId: &lhproto.StructDefId{Name: structDefName},
		Struct:      inlineStruct,
	}, nil
}

// goValueToInlineStruct converts a reflect.Value of a struct into an InlineStruct proto.
func goValueToInlineStruct(v reflect.Value) (*lhproto.InlineStruct, error) {
	t := v.Type()
	fields := make(map[string]*lhproto.StructField)

	for i := 0; i < t.NumField(); i++ {
		field := t.Field(i)
		if !field.IsExported() {
			continue
		}

		tag := field.Tag.Get("lh")
		if tag == "-" {
			continue
		}

		fieldName := tag
		if fieldName == "" {
			if jsonTag := field.Tag.Get("json"); jsonTag != "" {
				fieldName = strings.SplitN(jsonTag, ",", 2)[0]
			}
		}
		if fieldName == "" {
			fieldName = goFieldNameToCamelCase(field.Name)
		}

		fieldVal := v.Field(i)
		structField, err := goValueToStructField(fieldVal)
		if err != nil {
			return nil, fmt.Errorf("field %s: %w", field.Name, err)
		}

		fields[fieldName] = structField
	}

	return &lhproto.InlineStruct{Fields: fields}, nil
}

// goValueToStructField converts a Go value to a StructField proto.
func goValueToStructField(v reflect.Value) (*lhproto.StructField, error) {
	// Dereference pointer
	if v.Kind() == reflect.Ptr {
		if v.IsNil() {
			return &lhproto.StructField{
				Value: &lhproto.VariableValue{},
			}, nil
		}
		v = v.Elem()
	}

	// Handle nested structs with LHStructName
	if v.Kind() == reflect.Struct {
		structDefName := getStructDefName(v.Type())
		if structDefName != "" {
			inlineStruct, err := goValueToInlineStruct(v)
			if err != nil {
				return nil, err
			}
			return &lhproto.StructField{
				Value: &lhproto.VariableValue{
					Value: &lhproto.VariableValue_Struct{
						Struct: &lhproto.Struct{
							StructDefId: &lhproto.StructDefId{Name: structDefName},
							Struct:      inlineStruct,
						},
					},
				},
			}, nil
		}
	}

	// For primitive types, use InterfaceToVarVal
	varVal, err := InterfaceToVarVal(v.Interface())
	if err != nil {
		return nil, err
	}
	return &lhproto.StructField{Value: varVal}, nil
}

// StructProtoToGoStruct deserializes a Struct protobuf into a Go struct of the given type.
// The target type must be a pointer to a struct. This is the Go equivalent of Java's
// deserializeStructToObject() and .NET's DeserializeStructToObject().
func StructProtoToGoStruct(s *lhproto.Struct, targetType reflect.Type) (interface{}, error) {
	isPtr := false
	if targetType.Kind() == reflect.Ptr {
		isPtr = true
		targetType = targetType.Elem()
	}

	if targetType.Kind() != reflect.Struct {
		return nil, fmt.Errorf("target type must be a struct, got %s", targetType.Kind())
	}

	inlineStruct := s.GetStruct()
	if inlineStruct == nil {
		inlineStruct = &lhproto.InlineStruct{}
	}

	result, err := inlineStructToGoValue(inlineStruct, targetType)
	if err != nil {
		return nil, err
	}

	if isPtr {
		ptr := reflect.New(targetType)
		ptr.Elem().Set(result)
		return ptr.Interface(), nil
	}
	return result.Interface(), nil
}

// inlineStructToGoValue deserializes an InlineStruct into a reflect.Value of the given struct type.
func inlineStructToGoValue(inlineStruct *lhproto.InlineStruct, targetType reflect.Type) (reflect.Value, error) {
	result := reflect.New(targetType).Elem()
	fieldMap := inlineStruct.GetFields()

	for i := 0; i < targetType.NumField(); i++ {
		field := targetType.Field(i)
		if !field.IsExported() {
			continue
		}

		tag := field.Tag.Get("lh")
		if tag == "-" {
			continue
		}

		fieldName := tag
		if fieldName == "" {
			if jsonTag := field.Tag.Get("json"); jsonTag != "" {
				fieldName = strings.SplitN(jsonTag, ",", 2)[0]
			}
		}
		if fieldName == "" {
			fieldName = goFieldNameToCamelCase(field.Name)
		}

		structField, ok := fieldMap[fieldName]
		if !ok {
			continue // field not present in proto, leave as zero value
		}

		goVal, err := structFieldToGoValue(structField, field.Type)
		if err != nil {
			return reflect.Value{}, fmt.Errorf("field %s: %w", field.Name, err)
		}

		if goVal.IsValid() {
			result.Field(i).Set(goVal)
		}
	}

	return result, nil
}

// structFieldToGoValue converts a StructField proto into a Go reflect.Value of the target type.
func structFieldToGoValue(sf *lhproto.StructField, targetType reflect.Type) (reflect.Value, error) {
	varVal := sf.GetValue()
	if varVal == nil || varVal.GetValue() == nil {
		return reflect.Zero(targetType), nil
	}

	// Handle nested Struct values
	if structVal, ok := varVal.GetValue().(*lhproto.VariableValue_Struct); ok {
		elemType := targetType
		isPtr := false
		if elemType.Kind() == reflect.Ptr {
			isPtr = true
			elemType = elemType.Elem()
		}

		innerResult, err := inlineStructToGoValue(structVal.Struct.GetStruct(), elemType)
		if err != nil {
			return reflect.Value{}, err
		}

		if isPtr {
			ptr := reflect.New(elemType)
			ptr.Elem().Set(innerResult)
			return ptr, nil
		}
		return innerResult, nil
	}

	// For primitive types, use VarValToType
	val, err := VarValToType(varVal, targetType)
	if err != nil {
		return reflect.Value{}, err
	}
	return reflect.ValueOf(val), nil
}

// GetStructDefDependencies returns a topologically-sorted list of all StructDef types
// that the given struct type depends on (including itself). Each element is a reflect.Type
// of a struct that implements LHStructName().
func GetStructDefDependencies(structInstance interface{}) ([]reflect.Type, error) {
	t := reflect.TypeOf(structInstance)
	if t.Kind() == reflect.Ptr {
		t = t.Elem()
	}

	visited := make(map[reflect.Type]bool)
	var result []reflect.Type
	if err := collectStructDeps(t, visited, &result); err != nil {
		return nil, err
	}
	return result, nil
}

// collectStructDeps performs a DFS to collect struct dependencies in topological order.
func collectStructDeps(t reflect.Type, visited map[reflect.Type]bool, result *[]reflect.Type) error {
	if t.Kind() == reflect.Ptr {
		t = t.Elem()
	}
	if t.Kind() != reflect.Struct {
		return nil
	}

	structDefName := getStructDefName(t)
	if structDefName == "" {
		return nil // not a LH struct, skip
	}

	if visited[t] {
		return nil
	}
	visited[t] = true

	// First, recurse into dependencies
	for i := 0; i < t.NumField(); i++ {
		field := t.Field(i)
		if !field.IsExported() {
			continue
		}
		ft := field.Type
		if ft.Kind() == reflect.Ptr {
			ft = ft.Elem()
		}
		if ft.Kind() == reflect.Struct {
			if err := collectStructDeps(ft, visited, result); err != nil {
				return err
			}
		}
	}

	// Then add this type (dependencies come first)
	*result = append(*result, t)
	return nil
}

// RegisterStructDef registers a single Go struct as a StructDef with the LittleHorse server.
// The structInstance should be a zero value of the struct (e.g., MyStruct{}).
// The struct must implement LHStructName() string to provide the StructDef name.
//
// Nested struct dependencies are NOT automatically registered — callers must register
// them manually in dependency order (dependencies first).
func RegisterStructDef(
	client lhproto.LittleHorseClient,
	structInstance interface{},
	allowedUpdates *lhproto.StructDefCompatibilityType,
) error {
	t := reflect.TypeOf(structInstance)
	if t.Kind() == reflect.Ptr {
		t = t.Elem()
	}

	structDefName := getStructDefName(t)
	if structDefName == "" {
		return fmt.Errorf("struct type %s must implement LHStructName() string", t.Name())
	}

	inlineDef, err := GoStructToInlineStructDef(structInstance)
	if err != nil {
		return fmt.Errorf("failed to build InlineStructDef for %s: %w", structDefName, err)
	}

	updates := lhproto.StructDefCompatibilityType_NO_SCHEMA_UPDATES
	if allowedUpdates != nil {
		updates = *allowedUpdates
	}

	req := &lhproto.PutStructDefRequest{
		Name:           structDefName,
		StructDef:      inlineDef,
		AllowedUpdates: updates,
	}

	_, err = client.PutStructDef(context.Background(), req)
	if err != nil {
		return fmt.Errorf("failed to register StructDef '%s': %w", structDefName, err)
	}

	return nil
}
