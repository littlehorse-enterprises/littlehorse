package littlehorse

import (
	"context"
	"fmt"
	"reflect"
	"strings"
	"unicode"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
)

// LHStructDefInfo holds the metadata returned by the LHStructDef() method on Go structs.
// This is the Go equivalent of Java's @LHStructDef annotation and .NET's [LHStructDef] attribute.
type LHStructDefInfo struct {
	// Name is the StructDef name registered with the LittleHorse server.
	Name string
	// Description is an optional human-readable description of the StructDef.
	Description string
}

// lhTagInfo holds the parsed result of a Go struct field's "lh" tag.
// The tag format is: `lh:"fieldName,option1,option2,..."` (similar to encoding/json).
//
// Supported options:
//   - "masked": marks the field as containing sensitive information. Sets TypeDefinition.Masked = true
//     on the StructFieldDef, equivalent to Java's @LHStructField(masked = true) and
//     .NET's [LHStructField(masked: true)].
//   - "-": skips the field entirely (not included in the StructDef).
//
// Examples:
//
//	`lh:"firstName"`          // explicit field name, no options
//	`lh:"ssn,masked"`         // explicit field name + masked
//	`lh:",masked"`            // default field name + masked
//	`lh:"-"`                  // skip this field
type lhTagInfo struct {
	// name is the explicit field name from the tag (empty string means use fallback resolution).
	name string
	// masked is true when the "masked" option is present in the tag.
	masked bool
	// skip is true when the tag value is "-", meaning the field should be excluded.
	skip bool
}

// parseLHTag parses the "lh" struct tag value into an lhTagInfo.
// The format mirrors encoding/json: "name,opt1,opt2,...".
// A tag of "-" means the field should be skipped entirely.
// An empty tag returns all zero values (no name override, not masked, not skipped).
func parseLHTag(tag string) lhTagInfo {
	if tag == "-" {
		return lhTagInfo{skip: true}
	}

	parts := strings.Split(tag, ",")
	info := lhTagInfo{
		name: parts[0], // first element is always the field name (may be empty)
	}

	// Check remaining parts for known options.
	for _, opt := range parts[1:] {
		switch strings.TrimSpace(opt) {
		case "masked":
			info.masked = true
		}
	}

	return info
}

// resolveFieldName determines the final field name for a struct field by checking
// (in order): the lh tag name, the json tag name, or a PascalCase-to-camelCase conversion.
func resolveFieldName(lhName string, field reflect.StructField) string {
	if lhName != "" {
		return lhName
	}
	if jsonTag := field.Tag.Get("json"); jsonTag != "" {
		name := strings.SplitN(jsonTag, ",", 2)[0]
		if name != "" {
			return name
		}
	}
	return goFieldNameToCamelCase(field.Name)
}

// GoStructToInlineStructDef uses reflection to convert a Go struct instance into an
// InlineStructDef protobuf. The struct's exported fields are converted to StructFieldDef entries.
//
// Field names are resolved in order: "lh" struct tag, then "json" struct tag, then
// PascalCase-to-camelCase conversion of the Go field name. Use lh:"-" to skip a field.
// Use lh:"name,masked" to mark a field as containing sensitive data.
//
// Supported field types:
//   - string -> STR
//   - int, int16, int32, int64 -> INT
//   - float32, float64 -> DOUBLE
//   - bool -> BOOL
//   - []byte -> BYTES
//   - slices/arrays -> JSON_ARR
//   - structs with LHStructDef() -> nested StructDef reference
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

		// Parse the "lh" struct tag for field name, masked flag, and skip directive.
		tagInfo := parseLHTag(field.Tag.Get("lh"))
		if tagInfo.skip {
			continue
		}

		// Resolve field name: lh tag > json tag > PascalCase-to-camelCase.
		fieldName := resolveFieldName(tagInfo.name, field)

		fieldDef, err := goTypeToStructFieldDef(field.Type)
		if err != nil {
			return nil, fmt.Errorf("field %s: %w", field.Name, err)
		}

		// If the field is marked as masked via the "lh" tag (e.g., `lh:"ssn,masked"`),
		// set Masked = true on the field's TypeDefinition. This is the Go equivalent of
		// Java's @LHStructField(masked = true) and .NET's [LHStructField(masked: true)].
		if tagInfo.masked {
			fieldDef.FieldType.Masked = true
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
		info := getStructDefInfo(t)
		if info == nil {
			return nil, fmt.Errorf(
				"nested struct type %s must implement LHStructDef() method to be used as a StructDef field",
				t.Name(),
			)
		}
		return &lhproto.StructFieldDef{
			FieldType: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_StructDefId{
					StructDefId: &lhproto.StructDefId{
						Name: info.Name,
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

// getStructDefInfo returns the StructDef info for a Go type if it implements
// LHStructDef() LHStructDefInfo, or returns nil otherwise.
func getStructDefInfo(t reflect.Type) *LHStructDefInfo {
	infoType := reflect.TypeOf(LHStructDefInfo{})

	// Check if the type (or pointer to it) has an LHStructDef method.
	ptrType := reflect.PointerTo(t)
	method, ok := ptrType.MethodByName("LHStructDef")
	if !ok {
		method, ok = t.MethodByName("LHStructDef")
		if !ok {
			return nil
		}
		// Method on value receiver.
		if method.Type.NumOut() != 1 || method.Type.Out(0) != infoType {
			return nil
		}
		instance := reflect.New(t).Elem()
		results := method.Func.Call([]reflect.Value{instance})
		info := results[0].Interface().(LHStructDefInfo)
		return &info
	}

	// Method on pointer receiver.
	if method.Type.NumOut() != 1 || method.Type.Out(0) != infoType {
		return nil
	}
	instance := reflect.New(t)
	results := method.Func.Call([]reflect.Value{instance})
	info := results[0].Interface().(LHStructDefInfo)
	return &info
}

// getStructDefName is a convenience wrapper that returns just the name, or empty string.
func getStructDefName(t reflect.Type) string {
	info := getStructDefInfo(t)
	if info == nil {
		return ""
	}
	return info.Name
}

// GoStructToStructProto converts a Go struct instance into a Struct protobuf suitable for
// use as a VariableValue. The struct must implement LHStructDef() LHStructDefInfo.
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

	info := getStructDefInfo(t)
	if info == nil {
		return nil, fmt.Errorf("struct type %s must implement LHStructDef() LHStructDefInfo", t.Name())
	}

	inlineStruct, err := goValueToInlineStruct(v)
	if err != nil {
		return nil, err
	}

	return &lhproto.Struct{
		StructDefId: &lhproto.StructDefId{Name: info.Name},
		Struct:      inlineStruct,
	}, nil
}

// goValueToInlineStruct converts a reflect.Value of a struct into an InlineStruct proto.
// This uses the same tag parsing as buildInlineStructDef for consistent field name resolution.
func goValueToInlineStruct(v reflect.Value) (*lhproto.InlineStruct, error) {
	t := v.Type()
	fields := make(map[string]*lhproto.StructField)

	for i := 0; i < t.NumField(); i++ {
		field := t.Field(i)
		if !field.IsExported() {
			continue
		}

		// Parse the "lh" tag using the shared parser (handles "masked" and skip).
		tagInfo := parseLHTag(field.Tag.Get("lh"))
		if tagInfo.skip {
			continue
		}

		// Resolve field name consistently with buildInlineStructDef.
		fieldName := resolveFieldName(tagInfo.name, field)

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

	// Handle nested structs with LHStructDef
	if v.Kind() == reflect.Struct {
		info := getStructDefInfo(v.Type())
		if info != nil {
			inlineStruct, err := goValueToInlineStruct(v)
			if err != nil {
				return nil, err
			}
			return &lhproto.StructField{
				Value: &lhproto.VariableValue{
					Value: &lhproto.VariableValue_Struct{
						Struct: &lhproto.Struct{
							StructDefId: &lhproto.StructDefId{Name: info.Name},
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
// This uses the same tag parsing as buildInlineStructDef for consistent field name resolution.
func inlineStructToGoValue(inlineStruct *lhproto.InlineStruct, targetType reflect.Type) (reflect.Value, error) {
	result := reflect.New(targetType).Elem()
	fieldMap := inlineStruct.GetFields()

	for i := 0; i < targetType.NumField(); i++ {
		field := targetType.Field(i)
		if !field.IsExported() {
			continue
		}

		// Parse the "lh" tag using the shared parser (handles "masked" and skip).
		tagInfo := parseLHTag(field.Tag.Get("lh"))
		if tagInfo.skip {
			continue
		}

		// Resolve field name consistently with buildInlineStructDef.
		fieldName := resolveFieldName(tagInfo.name, field)

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
// of a struct that implements LHStructDef().
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
// The struct must implement LHStructDef() LHStructDefInfo to provide the StructDef name and description.
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

	info := getStructDefInfo(t)
	if info == nil {
		return fmt.Errorf("struct type %s must implement LHStructDef() LHStructDefInfo", t.Name())
	}

	inlineDef, err := GoStructToInlineStructDef(structInstance)
	if err != nil {
		return fmt.Errorf("failed to build InlineStructDef for %s: %w", info.Name, err)
	}

	updates := lhproto.StructDefCompatibilityType_NO_SCHEMA_UPDATES
	if allowedUpdates != nil {
		updates = *allowedUpdates
	}

	req := &lhproto.PutStructDefRequest{
		Name:           info.Name,
		StructDef:      inlineDef,
		AllowedUpdates: updates,
	}
	if info.Description != "" {
		req.Description = &info.Description
	}

	_, err = client.PutStructDef(context.Background(), req)
	if err != nil {
		return fmt.Errorf("failed to register StructDef '%s': %w", info.Name, err)
	}

	return nil
}
