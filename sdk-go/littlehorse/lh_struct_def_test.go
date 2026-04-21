package littlehorse_test

import (
	"reflect"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/stretchr/testify/assert"
)

// --- Test structs ---

type SimpleStruct struct {
	Name  string  `json:"name"`
	Age   int     `json:"age"`
	Score float64 `json:"score"`
	Valid bool    `json:"valid"`
}

func (SimpleStruct) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "simple-struct"}
}

type StructWithLHTag struct {
	FirstName string `lh:"first_name"`
	LastName  string `lh:"last_name" json:"ignored"`
}

func (StructWithLHTag) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "lh-tag-struct"}
}

type StructWithJSONTag struct {
	MyField string `json:"my_field"`
}

func (StructWithJSONTag) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "json-tag-struct"}
}

type StructWithNoTags struct {
	FirstName  string
	HTTPServer string
	ZipCode    int
}

func (StructWithNoTags) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "no-tag-struct"}
}

type StructWithIgnoredField struct {
	Name    string `json:"name"`
	Secret  string `lh:"-"`
	Another string `json:"another"`
}

func (StructWithIgnoredField) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "ignored-field-struct"}
}

type StructWithBytes struct {
	Data    []byte `json:"data"`
	Payload []byte `json:"payload"`
}

func (StructWithBytes) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "bytes-struct"}
}

type StructWithSlice struct {
	Tags   []string `json:"tags"`
	Scores []int    `json:"scores"`
}

func (StructWithSlice) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "slice-struct"}
}

type NestedAddress struct {
	Street string `json:"street"`
	City   string `json:"city"`
}

func (NestedAddress) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "nested-address"}
}

type StructWithNested struct {
	Name    string        `json:"name"`
	Address NestedAddress `json:"address"`
}

func (StructWithNested) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "nested-struct"}
}

type StructWithPointerNested struct {
	Name    string         `json:"name"`
	Address *NestedAddress `json:"address"`
}

func (StructWithPointerNested) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "ptr-nested-struct"}
}

type StructWithUnexportedField struct {
	Name    string `json:"name"`
	private string //nolint:unused
}

func (StructWithUnexportedField) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "unexported-field-struct"}
}

// A nested struct that does NOT implement LHStructDef.
type PlainNestedStruct struct {
	Value string
}

type StructWithPlainNested struct {
	Name   string            `json:"name"`
	Nested PlainNestedStruct `json:"nested"`
}

func (StructWithPlainNested) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "plain-nested-struct"}
}

type StructWithIntTypes struct {
	Int16Val int16 `json:"int16Val"`
	Int32Val int32 `json:"int32Val"`
	Int64Val int64 `json:"int64Val"`
	IntVal   int   `json:"intVal"`
}

func (StructWithIntTypes) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "int-types-struct"}
}

type StructWithFloatTypes struct {
	Float32Val float32 `json:"float32Val"`
	Float64Val float64 `json:"float64Val"`
}

func (StructWithFloatTypes) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "float-types-struct"}
}

// StructWithMaskedField demonstrates the lh:"name,masked" tag syntax.
// The SSN field is marked as masked because it contains sensitive information.
type StructWithMaskedField struct {
	Name string `json:"name"`
	SSN  string `lh:"ssn,masked"`
	Age  int    `json:"age"`
}

func (StructWithMaskedField) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "masked-field-struct"}
}

// StructWithMaskedAndDefaultName uses lh:",masked" to mask a field while
// falling back to the default camelCase name resolution.
type StructWithMaskedAndDefaultName struct {
	SocialSecurityNumber string `lh:",masked"`
	Name                 string `json:"name"`
}

func (StructWithMaskedAndDefaultName) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "masked-default-name-struct"}
}

// StructWithMultipleMaskedFields has several masked fields.
type StructWithMultipleMaskedFields struct {
	PublicName string `json:"publicName"`
	SSN        string `lh:"ssn,masked"`
	CreditCard string `lh:"creditCard,masked"`
	Phone      string `json:"phone"`
}

func (StructWithMultipleMaskedFields) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "multi-masked-struct"}
}

// StructWithMaskedNestedField has a nested struct field marked as masked.
type StructWithMaskedNestedField struct {
	Name          string        `json:"name"`
	SecretAddress NestedAddress `lh:"secretAddress,masked"`
}

func (StructWithMaskedNestedField) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "masked-nested-struct"}
}

// StructWithLHAndJSON has an lh tag for both name and masked, plus a json tag
// that should be ignored when lh name is present.
type StructWithLHAndJSON struct {
	Field1 string `lh:"lhName" json:"jsonName"`
	Field2 string `lh:",masked" json:"field2Json"`
	Field3 string `json:"field3Json"`
}

func (StructWithLHAndJSON) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "lh-and-json-struct"}
}

// StructWithDescription tests that LHStructDefInfo.Description is propagated.
type StructWithDescriptionExternal struct {
	Value string `json:"value"`
}

func (StructWithDescriptionExternal) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{
		Name:        "described-struct",
		Description: "A struct with a description for testing",
	}
}

// StructWithMixedTags exercises multiple tag combinations in one struct:
// explicit lh name, masked-only lh, lh skip, json-only, and no tags.
type StructWithMixedTags struct {
	ExplicitLH string `lh:"custom"`
	MaskedOnly string `lh:",masked"`
	Skipped    string `lh:"-"`
	JSONOnly   string `json:"jsonField"`
	NoTags     string
}

func (StructWithMixedTags) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "mixed-tags-struct"}
}

// --- Tests for GoStructToInlineStructDef ---

func TestGoStructToInlineStructDef_SimplePrimitiveTypes(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(SimpleStruct{})
	assert.Nil(t, err)
	assert.NotNil(t, def)
	assert.Len(t, def.Fields, 4)

	assert.Equal(t,
		lhproto.VariableType_STR,
		def.Fields["name"].FieldType.GetPrimitiveType(),
	)
	assert.Equal(t,
		lhproto.VariableType_INT,
		def.Fields["age"].FieldType.GetPrimitiveType(),
	)
	assert.Equal(t,
		lhproto.VariableType_DOUBLE,
		def.Fields["score"].FieldType.GetPrimitiveType(),
	)
	assert.Equal(t,
		lhproto.VariableType_BOOL,
		def.Fields["valid"].FieldType.GetPrimitiveType(),
	)
}

func TestGoStructToInlineStructDef_LHTagTakesPrecedence(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithLHTag{})
	assert.Nil(t, err)

	// lh tag should be used; json tag should be ignored when lh tag is present.
	assert.Contains(t, def.Fields, "first_name")
	assert.Contains(t, def.Fields, "last_name")
	assert.NotContains(t, def.Fields, "ignored")
}

func TestGoStructToInlineStructDef_JSONTagFallback(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithJSONTag{})
	assert.Nil(t, err)
	assert.Contains(t, def.Fields, "my_field")
	assert.NotContains(t, def.Fields, "MyField")
}

func TestGoStructToInlineStructDef_PascalCaseToCamelCase(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithNoTags{})
	assert.Nil(t, err)

	assert.Contains(t, def.Fields, "firstName")
	assert.Contains(t, def.Fields, "httpServer")
	assert.Contains(t, def.Fields, "zipCode")
}

func TestGoStructToInlineStructDef_IgnoredField(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithIgnoredField{})
	assert.Nil(t, err)
	assert.Len(t, def.Fields, 2)
	assert.Contains(t, def.Fields, "name")
	assert.Contains(t, def.Fields, "another")
	assert.NotContains(t, def.Fields, "Secret")
}

func TestGoStructToInlineStructDef_ByteSlice(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithBytes{})
	assert.Nil(t, err)
	assert.Equal(t,
		lhproto.VariableType_BYTES,
		def.Fields["data"].FieldType.GetPrimitiveType(),
	)
	assert.Equal(t,
		lhproto.VariableType_BYTES,
		def.Fields["payload"].FieldType.GetPrimitiveType(),
	)
}

func TestGoStructToInlineStructDef_SliceMapsToJsonArr(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithSlice{})
	assert.Nil(t, err)
	assert.Equal(t,
		lhproto.VariableType_JSON_ARR,
		def.Fields["tags"].FieldType.GetPrimitiveType(),
	)
	assert.Equal(t,
		lhproto.VariableType_JSON_ARR,
		def.Fields["scores"].FieldType.GetPrimitiveType(),
	)
}

func TestGoStructToInlineStructDef_NestedStructWithLHStructDef(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithNested{})
	assert.Nil(t, err)
	assert.Len(t, def.Fields, 2)

	addressField := def.Fields["address"]
	assert.NotNil(t, addressField)
	assert.Equal(t, "nested-address", addressField.FieldType.GetStructDefId().GetName())
}

func TestGoStructToInlineStructDef_PointerNestedStruct(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithPointerNested{})
	assert.Nil(t, err)
	assert.Len(t, def.Fields, 2)

	addressField := def.Fields["address"]
	assert.NotNil(t, addressField)
	assert.Equal(t, "nested-address", addressField.FieldType.GetStructDefId().GetName())
}

func TestGoStructToInlineStructDef_UnexportedFieldsSkipped(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithUnexportedField{})
	assert.Nil(t, err)
	assert.Len(t, def.Fields, 1)
	assert.Contains(t, def.Fields, "name")
}

func TestGoStructToInlineStructDef_NestedStructWithoutLHStructDef_ReturnsError(t *testing.T) {
	_, err := littlehorse.GoStructToInlineStructDef(StructWithPlainNested{})
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "must implement LHStructDef()")
}

func TestGoStructToInlineStructDef_NonStructInput_ReturnsError(t *testing.T) {
	_, err := littlehorse.GoStructToInlineStructDef("not-a-struct")
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "expected a struct type")
}

func TestGoStructToInlineStructDef_PointerToStruct(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(&SimpleStruct{})
	assert.Nil(t, err)
	assert.Len(t, def.Fields, 4)
}

func TestGoStructToInlineStructDef_IntTypes(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithIntTypes{})
	assert.Nil(t, err)

	for _, fieldName := range []string{"int16Val", "int32Val", "int64Val", "intVal"} {
		assert.Equal(t,
			lhproto.VariableType_INT,
			def.Fields[fieldName].FieldType.GetPrimitiveType(),
			"field %s should be INT", fieldName,
		)
	}
}

func TestGoStructToInlineStructDef_FloatTypes(t *testing.T) {
	def, err := littlehorse.GoStructToInlineStructDef(StructWithFloatTypes{})
	assert.Nil(t, err)

	for _, fieldName := range []string{"float32Val", "float64Val"} {
		assert.Equal(t,
			lhproto.VariableType_DOUBLE,
			def.Fields[fieldName].FieldType.GetPrimitiveType(),
			"field %s should be DOUBLE", fieldName,
		)
	}
}

// --- Tests for masked fields via lh struct tag ---

func TestGoStructToInlineStructDef_MaskedField(t *testing.T) {
	// A field tagged with lh:"ssn,masked" should have TypeDefinition.Masked = true.
	def, err := littlehorse.GoStructToInlineStructDef(StructWithMaskedField{})
	assert.Nil(t, err)
	assert.Len(t, def.Fields, 3)

	// The "ssn" field should be masked.
	ssnField := def.Fields["ssn"]
	assert.NotNil(t, ssnField)
	assert.True(t, ssnField.FieldType.Masked, "ssn field should be masked")
	assert.Equal(t, lhproto.VariableType_STR, ssnField.FieldType.GetPrimitiveType())

	// The "name" field should NOT be masked.
	nameField := def.Fields["name"]
	assert.NotNil(t, nameField)
	assert.False(t, nameField.FieldType.Masked, "name field should not be masked")

	// The "age" field should NOT be masked.
	ageField := def.Fields["age"]
	assert.NotNil(t, ageField)
	assert.False(t, ageField.FieldType.Masked, "age field should not be masked")
}

func TestGoStructToInlineStructDef_MaskedWithDefaultName(t *testing.T) {
	// A field tagged with lh:",masked" should be masked and use the default camelCase name.
	def, err := littlehorse.GoStructToInlineStructDef(StructWithMaskedAndDefaultName{})
	assert.Nil(t, err)

	// Should use PascalCase-to-camelCase since lh tag name is empty.
	ssnField := def.Fields["socialSecurityNumber"]
	assert.NotNil(t, ssnField, "expected field 'socialSecurityNumber' from camelCase conversion")
	assert.True(t, ssnField.FieldType.Masked, "socialSecurityNumber field should be masked")

	nameField := def.Fields["name"]
	assert.NotNil(t, nameField)
	assert.False(t, nameField.FieldType.Masked, "name field should not be masked")
}

func TestGoStructToInlineStructDef_MultipleMaskedFields(t *testing.T) {
	// Multiple fields can be masked independently.
	def, err := littlehorse.GoStructToInlineStructDef(StructWithMultipleMaskedFields{})
	assert.Nil(t, err)

	assert.True(t, def.Fields["ssn"].FieldType.Masked, "ssn should be masked")
	assert.True(t, def.Fields["creditCard"].FieldType.Masked, "creditCard should be masked")
	assert.False(t, def.Fields["publicName"].FieldType.Masked, "publicName should not be masked")
	assert.False(t, def.Fields["phone"].FieldType.Masked, "phone should not be masked")
}

func TestGoStructToInlineStructDef_NonMaskedFieldsDefaultToFalse(t *testing.T) {
	// Fields without a "masked" tag option should have Masked = false (the proto default).
	def, err := littlehorse.GoStructToInlineStructDef(SimpleStruct{})
	assert.Nil(t, err)

	for fieldName, fieldDef := range def.Fields {
		assert.False(t, fieldDef.FieldType.Masked, "field %s should not be masked", fieldName)
	}
}

// --- Tests for masked fields: serialization/deserialization round-trip ---

func TestToLhStruct_MaskedFieldRoundTrip(t *testing.T) {
	// Ensure that masked fields serialize and deserialize values correctly.
	// Masking is a metadata concern on the StructDef, not on the Struct value itself.
	original := StructWithMaskedField{Name: "Alice", SSN: "123-45-6789", Age: 30}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	// Values should be present in the serialized proto (masking is enforced server-side).
	fields := proto.Struct.GetFields()
	assert.Equal(t, "Alice", fields["name"].Value.GetStr())
	assert.Equal(t, "123-45-6789", fields["ssn"].Value.GetStr())
	assert.Equal(t, int64(30), fields["age"].Value.GetInt())

	// Round-trip: deserialize back to a Go struct.
	result, err := littlehorse.StructProtoToGoStruct(proto, reflect.TypeOf(StructWithMaskedField{}))
	assert.Nil(t, err)

	restored := result.(StructWithMaskedField)
	assert.Equal(t, original, restored)
}

// --- Tests for DeclareStruct in workflow compilation ---

func TestDeclareStructAddsStructDefIdVariable(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		thread.DeclareStruct("my-person", "person")
	}, "struct-test")

	putWf, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	assert.Len(t, entrypoint.VariableDefs, 1)

	varDef := entrypoint.VariableDefs[0]
	assert.Equal(t, "my-person", varDef.VarDef.Name)

	// The key check: the TypeDefinition should use StructDefId, not PrimitiveType.
	structDefId := varDef.VarDef.TypeDef.GetStructDefId()
	assert.NotNil(t, structDefId, "TypeDefinition should have StructDefId set")
	assert.Equal(t, "person", structDefId.GetName())
	assert.Equal(t, int32(-1), structDefId.GetVersion())
}

func TestDeclareStructWithExplicitVersionAddsVersionedStructDefId(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		thread.DeclareStructWithVersion("my-person", "person", 3)
	}, "struct-version-test")

	putWf, _ := wf.Compile()

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]

	varDef := entrypoint.VariableDefs[0]
	structDefId := varDef.VarDef.TypeDef.GetStructDefId()
	assert.NotNil(t, structDefId, "TypeDefinition should have StructDefId set")
	assert.Equal(t, "person", structDefId.GetName())
	assert.Equal(t, int32(3), structDefId.GetVersion())
}

func TestDeclareStructVariableIsPrivateByDefault(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		thread.DeclareStruct("my-var", "my-struct-def")
	}, "struct-access-test")

	putWf, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	varDef := entrypoint.VariableDefs[0]
	assert.Equal(t, lhproto.WfRunVariableAccessLevel_PRIVATE_VAR, varDef.AccessLevel)
}

func TestDeclareMultipleStructVariables(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		thread.DeclareStruct("person-var", "person")
		thread.DeclareStruct("address-var", "address")
		thread.DeclareStr("normal-str")
	}, "multi-struct-test")

	putWf, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	assert.Len(t, entrypoint.VariableDefs, 3)

	// First var is struct with StructDefId
	assert.Equal(t, "person-var", entrypoint.VariableDefs[0].VarDef.Name)
	assert.Equal(t, "person", entrypoint.VariableDefs[0].VarDef.TypeDef.GetStructDefId().GetName())
	assert.Equal(t, int32(-1), entrypoint.VariableDefs[0].VarDef.TypeDef.GetStructDefId().GetVersion())

	// Second var is struct with StructDefId
	assert.Equal(t, "address-var", entrypoint.VariableDefs[1].VarDef.Name)
	assert.Equal(t, "address", entrypoint.VariableDefs[1].VarDef.TypeDef.GetStructDefId().GetName())
	assert.Equal(t, int32(-1), entrypoint.VariableDefs[1].VarDef.TypeDef.GetStructDefId().GetVersion())

	// Third var is a regular STR
	assert.Equal(t, "normal-str", entrypoint.VariableDefs[2].VarDef.Name)
	assert.Equal(t,
		lhproto.VariableType_STR,
		entrypoint.VariableDefs[2].VarDef.TypeDef.GetPrimitiveType(),
	)
}

func TestDeclareStructCanBePassedAsTaskInput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		personVar := thread.DeclareStruct("my-person", "person")
		thread.Execute("process-person", personVar)
	}, "struct-task-input-test")

	putWf, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-process-person-TASK"]
	assert.NotNil(t, taskNode)

	taskDef := taskNode.GetTask()
	assert.NotNil(t, taskDef)
	assert.Len(t, taskDef.Variables, 1)
	assert.Equal(t, "my-person", taskDef.Variables[0].GetVariableName())
}

func TestDeclareStructSearchable(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		thread.DeclareStruct("my-struct", "my-struct-def").Searchable()
	}, "struct-searchable-test")

	putWf, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	varDef := entrypoint.VariableDefs[0]
	assert.Equal(t, "my-struct", varDef.VarDef.Name)
	assert.True(t, varDef.Searchable)
	assert.Equal(t, "my-struct-def", varDef.VarDef.TypeDef.GetStructDefId().GetName())
	assert.Equal(t, int32(-1), varDef.VarDef.TypeDef.GetStructDefId().GetVersion())
}

// --- Tests for ReflectTypeToTypeDef ---

func TestReflectTypeToTypeDef_StructWithLHStructDef(t *testing.T) {
	rt := reflect.TypeOf(SimpleStruct{})
	typeDef := littlehorse.ReflectTypeToTypeDef(rt)

	assert.NotNil(t, typeDef.GetStructDefId())
	assert.Equal(t, "simple-struct", typeDef.GetStructDefId().GetName())
	assert.Equal(t, int32(-1), typeDef.GetStructDefId().GetVersion())
}

func TestReflectTypeToTypeDef_PointerToStruct(t *testing.T) {
	rt := reflect.TypeOf(&SimpleStruct{})
	typeDef := littlehorse.ReflectTypeToTypeDef(rt)

	assert.NotNil(t, typeDef.GetStructDefId())
	assert.Equal(t, "simple-struct", typeDef.GetStructDefId().GetName())
	assert.Equal(t, int32(-1), typeDef.GetStructDefId().GetVersion())
}

func TestReflectTypeToTypeDef_PrimitiveTypes(t *testing.T) {
	tests := []struct {
		value    interface{}
		expected lhproto.VariableType
	}{
		{"hello", lhproto.VariableType_STR},
		{42, lhproto.VariableType_INT},
		{3.14, lhproto.VariableType_DOUBLE},
		{true, lhproto.VariableType_BOOL},
	}

	for _, tt := range tests {
		rt := reflect.TypeOf(tt.value)
		typeDef := littlehorse.ReflectTypeToTypeDef(rt)
		assert.Equal(t, tt.expected, typeDef.GetPrimitiveType(), "for type %T", tt.value)
	}
}

func TestReflectTypeToTypeDef_PlainStructFallsBackToJsonObj(t *testing.T) {
	// PlainNestedStruct does not implement LHStructDef, so it should fall back to JSON_OBJ
	rt := reflect.TypeOf(PlainNestedStruct{})
	typeDef := littlehorse.ReflectTypeToTypeDef(rt)

	assert.Equal(t, lhproto.VariableType_JSON_OBJ, typeDef.GetPrimitiveType())
}

// --- Tests for ToLhStruct and StructProtoToGoStruct ---

func TestToLhStruct_SimpleStruct(t *testing.T) {
	s := SimpleStruct{Name: "Alice", Age: 30, Score: 95.5, Valid: true}
	proto, err := littlehorse.ToLhStruct(s)
	assert.Nil(t, err)
	assert.Equal(t, "simple-struct", proto.StructDefId.GetName())
	assert.Equal(t, int32(-1), proto.StructDefId.GetVersion())

	fields := proto.Struct.GetFields()
	assert.Equal(t, "Alice", fields["name"].Value.GetStr())
	assert.Equal(t, int64(30), fields["age"].Value.GetInt())
	assert.Equal(t, 95.5, fields["score"].Value.GetDouble())
	assert.Equal(t, true, fields["valid"].Value.GetBool())
}

func TestToLhStruct_NestedStruct(t *testing.T) {
	s := StructWithNested{
		Name: "Bob",
		Address: NestedAddress{
			Street: "123 Main St",
			City:   "Springfield",
		},
	}
	proto, err := littlehorse.ToLhStruct(s)
	assert.Nil(t, err)

	fields := proto.Struct.GetFields()
	assert.Equal(t, "Bob", fields["name"].Value.GetStr())

	nestedStruct := fields["address"].Value.GetStruct()
	assert.NotNil(t, nestedStruct)
	assert.Equal(t, "nested-address", nestedStruct.StructDefId.GetName())
	assert.Equal(t, "123 Main St", nestedStruct.Struct.GetFields()["street"].Value.GetStr())
	assert.Equal(t, "Springfield", nestedStruct.Struct.GetFields()["city"].Value.GetStr())
}

func TestToLhStruct_RequiresLHStructDef(t *testing.T) {
	_, err := littlehorse.ToLhStruct(PlainNestedStruct{Value: "test"})
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), "must implement LHStructDef()")
}

func TestStructProtoToGoStruct_SimpleStruct(t *testing.T) {
	// First serialize, then deserialize
	original := SimpleStruct{Name: "Alice", Age: 30, Score: 95.5, Valid: true}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	result, err := littlehorse.StructProtoToGoStruct(proto, reflect.TypeOf(SimpleStruct{}))
	assert.Nil(t, err)

	restored := result.(SimpleStruct)
	assert.Equal(t, original, restored)
}

func TestStructProtoToGoStruct_PointerTarget(t *testing.T) {
	original := SimpleStruct{Name: "Alice", Age: 30, Score: 95.5, Valid: true}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	result, err := littlehorse.StructProtoToGoStruct(proto, reflect.TypeOf(&SimpleStruct{}))
	assert.Nil(t, err)

	restored := result.(*SimpleStruct)
	assert.Equal(t, "Alice", restored.Name)
	assert.Equal(t, 30, restored.Age)
}

func TestStructProtoToGoStruct_NestedStruct(t *testing.T) {
	original := StructWithNested{
		Name:    "Bob",
		Address: NestedAddress{Street: "123 Main St", City: "Springfield"},
	}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	result, err := littlehorse.StructProtoToGoStruct(proto, reflect.TypeOf(StructWithNested{}))
	assert.Nil(t, err)

	restored := result.(StructWithNested)
	assert.Equal(t, original, restored)
}

// --- Tests for InterfaceToVarVal with StructDef types ---

func TestInterfaceToVarVal_StructWithLHStructDef(t *testing.T) {
	s := SimpleStruct{Name: "Alice", Age: 30, Score: 95.5, Valid: true}
	varVal, err := littlehorse.InterfaceToVarVal(s)
	assert.Nil(t, err)

	// Should be serialized as a Struct, not as JSON_OBJ
	structVal := varVal.GetStruct()
	assert.NotNil(t, structVal, "expected Struct VariableValue, got %T", varVal.GetValue())

	assert.Equal(t, "simple-struct", structVal.StructDefId.GetName())
	assert.Equal(t, "Alice", structVal.Struct.GetFields()["name"].Value.GetStr())
}

func TestInterfaceToVarVal_PlainStructStillUsesJsonObj(t *testing.T) {
	s := PlainNestedStruct{Value: "test"}
	varVal, err := littlehorse.InterfaceToVarVal(s)
	assert.Nil(t, err)

	// Should still be JSON_OBJ since PlainNestedStruct doesn't implement LHStructDef
	assert.NotEmpty(t, varVal.GetJsonObj())
}

// --- Tests for VarValToType with Struct VariableValue ---

func TestVarValToType_StructToPointer(t *testing.T) {
	original := SimpleStruct{Name: "Alice", Age: 30, Score: 95.5, Valid: true}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	varVal := &lhproto.VariableValue{
		Value: &lhproto.VariableValue_Struct{Struct: proto},
	}

	result, err := littlehorse.VarValToType(varVal, reflect.TypeOf(&SimpleStruct{}))
	assert.Nil(t, err)

	restored := result.(*SimpleStruct)
	assert.Equal(t, original.Name, restored.Name)
	assert.Equal(t, original.Age, restored.Age)
}

func TestVarValToType_StructToValue(t *testing.T) {
	original := SimpleStruct{Name: "Bob", Age: 25, Score: 80.0, Valid: false}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	varVal := &lhproto.VariableValue{
		Value: &lhproto.VariableValue_Struct{Struct: proto},
	}

	result, err := littlehorse.VarValToType(varVal, reflect.TypeOf(SimpleStruct{}))
	assert.Nil(t, err)

	restored := result.(SimpleStruct)
	assert.Equal(t, original, restored)
}

// --- Tests for lh tag + json tag interaction ---

func TestGoStructToInlineStructDef_MixedTags(t *testing.T) {
	// StructWithMixedTags exercises all tag combination scenarios in a single struct.
	structDef, err := littlehorse.GoStructToInlineStructDef(StructWithMixedTags{})
	assert.Nil(t, err)

	// ExplicitLH should use "custom" from lh tag
	assert.Contains(t, structDef.Fields, "custom", "expected field named 'custom' from lh tag")
	// MaskedOnly has lh:",masked" so no lh name; no json tag; falls back to camelCase
	assert.Contains(t, structDef.Fields, "maskedOnly", "expected field named 'maskedOnly' from camelCase fallback")
	// Skipped has lh:"-" so must not appear
	assert.NotContains(t, structDef.Fields, "skipped", "skipped field should not appear")
	assert.NotContains(t, structDef.Fields, "Skipped", "skipped field should not appear")
	// JSONOnly has json:"jsonField" and no lh tag; uses json name
	assert.Contains(t, structDef.Fields, "jsonField", "expected field named 'jsonField' from json tag")
	// NoTags has no lh or json tag; falls back to camelCase of "NoTags" -> "noTags"
	assert.Contains(t, structDef.Fields, "noTags", "expected field named 'noTags' from camelCase fallback")

	// Exactly 4 fields should be present (5 declared minus 1 skipped)
	assert.Len(t, structDef.Fields, 4)
}

func TestGoStructToInlineStructDef_MixedTags_MaskedCorrectness(t *testing.T) {
	structDef, err := littlehorse.GoStructToInlineStructDef(StructWithMixedTags{})
	assert.Nil(t, err)

	// Only "maskedOnly" should be masked
	assert.True(t, structDef.Fields["maskedOnly"].FieldType.Masked, "maskedOnly should be masked")
	assert.False(t, structDef.Fields["custom"].FieldType.Masked, "custom should not be masked")
	assert.False(t, structDef.Fields["jsonField"].FieldType.Masked, "jsonField should not be masked")
	assert.False(t, structDef.Fields["noTags"].FieldType.Masked, "noTags should not be masked")
}

func TestGoStructToInlineStructDef_LHNameOverridesJSON(t *testing.T) {
	// StructWithLHAndJSON has `lh:"lhName" json:"jsonName"` on Field1.
	structDef, err := littlehorse.GoStructToInlineStructDef(StructWithLHAndJSON{})
	assert.Nil(t, err)

	// Field1: lh:"lhName" must override json:"jsonName"
	assert.Contains(t, structDef.Fields, "lhName", "lh tag name should take priority over json tag")
	assert.NotContains(t, structDef.Fields, "jsonName", "json tag name should not be used when lh name is present")

	// Field2: lh:",masked" with json:"field2Json" -> name from json, masked = true
	assert.Contains(t, structDef.Fields, "field2Json", "empty lh name should fall back to json")
	assert.True(t, structDef.Fields["field2Json"].FieldType.Masked, "field with lh:',masked' should be masked")

	// Field3: json:"field3Json" only -> name from json, not masked
	assert.Contains(t, structDef.Fields, "field3Json", "json tag name should be used when no lh tag")
	assert.False(t, structDef.Fields["field3Json"].FieldType.Masked, "field without masked should not be masked")
}

func TestGoStructToInlineStructDef_MaskedNestedField(t *testing.T) {
	// StructWithMaskedNestedField has a nested struct field marked as masked.
	structDef, err := littlehorse.GoStructToInlineStructDef(StructWithMaskedNestedField{})
	assert.Nil(t, err)

	assert.Contains(t, structDef.Fields, "secretAddress")
	assert.True(t, structDef.Fields["secretAddress"].FieldType.Masked,
		"nested struct field with lh:'...,masked' should be masked")
	// The nested struct should still resolve as a STRUCT type reference
	assert.NotNil(t, structDef.Fields["secretAddress"].FieldType.GetStructDefId(),
		"nested struct field should still have its struct def id")
}

// --- Tests for StructWithDescription ---

func TestToLhStruct_Description(t *testing.T) {
	// StructWithDescriptionExternal has a description in its LHStructDefInfo.
	// The description is used in RegisterStructDef; here we just verify the
	// struct serialization still works correctly.
	s := StructWithDescriptionExternal{Value: "test"}
	proto, err := littlehorse.ToLhStruct(s)
	assert.Nil(t, err)
	assert.Equal(t, "test", proto.Struct.GetFields()["value"].Value.GetStr())
	assert.Equal(t, "described-struct", proto.StructDefId.GetName())
}

// --- Tests for round-trip serialization with lh tags ---

func TestToLhStruct_LHTagRoundTrip(t *testing.T) {
	// Serialize a struct with lh tags and verify field names come from the lh tag.
	original := StructWithLHAndJSON{
		Field1: "hello",
		Field2: "secret",
		Field3: "world",
	}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	fields := proto.Struct.GetFields()
	// Field1 should be keyed by "lhName" (from lh tag), not "jsonName"
	assert.Contains(t, fields, "lhName")
	assert.NotContains(t, fields, "jsonName")
	assert.Equal(t, "hello", fields["lhName"].Value.GetStr())

	// Field2 should be keyed by "field2Json" (lh has empty name, falls back to json)
	assert.Contains(t, fields, "field2Json")
	assert.Equal(t, "secret", fields["field2Json"].Value.GetStr())

	// Field3 should be keyed by "field3Json" (json tag)
	assert.Contains(t, fields, "field3Json")
	assert.Equal(t, "world", fields["field3Json"].Value.GetStr())
}

func TestStructProtoToGoStruct_LHTagRoundTrip(t *testing.T) {
	// Full round-trip: Go struct -> proto -> Go struct with lh tags.
	original := StructWithLHAndJSON{
		Field1: "hello",
		Field2: "secret",
		Field3: "world",
	}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	result, err := littlehorse.StructProtoToGoStruct(proto, reflect.TypeOf(StructWithLHAndJSON{}))
	assert.Nil(t, err)

	restored := result.(StructWithLHAndJSON)
	assert.Equal(t, original, restored)
}

func TestMaskedFieldSerializationRoundTrip_NestedStruct(t *testing.T) {
	// Round-trip a struct with a masked nested struct field.
	original := StructWithMaskedNestedField{
		Name:          "Alice",
		SecretAddress: NestedAddress{Street: "123 Secret St", City: "Hidden City"},
	}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	result, err := littlehorse.StructProtoToGoStruct(proto, reflect.TypeOf(StructWithMaskedNestedField{}))
	assert.Nil(t, err)

	restored := result.(StructWithMaskedNestedField)
	assert.Equal(t, original, restored)
}

func TestMixedTagsSerializationRoundTrip(t *testing.T) {
	// Round-trip a struct with mixed tag scenarios. Note: the "Skipped" field
	// will be lost in the round trip since it is excluded by lh:"-".
	original := StructWithMixedTags{
		ExplicitLH: "custom_value",
		MaskedOnly: "masked_value",
		Skipped:    "this should be lost",
		JSONOnly:   "json_value",
		NoTags:     "no_tag_value",
	}
	proto, err := littlehorse.ToLhStruct(original)
	assert.Nil(t, err)

	result, err := littlehorse.StructProtoToGoStruct(proto, reflect.TypeOf(StructWithMixedTags{}))
	assert.Nil(t, err)

	restored := result.(StructWithMixedTags)
	assert.Equal(t, "custom_value", restored.ExplicitLH)
	assert.Equal(t, "masked_value", restored.MaskedOnly)
	assert.Equal(t, "", restored.Skipped, "skipped field should be zero-value after round-trip")
	assert.Equal(t, "json_value", restored.JSONOnly)
	assert.Equal(t, "no_tag_value", restored.NoTags)
}
