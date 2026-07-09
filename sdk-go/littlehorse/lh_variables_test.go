package littlehorse_test

import (
	"reflect"
	"testing"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/stretchr/testify/assert"
)

func TestStrToVarValWithStr(t *testing.T) {
	result, err := littlehorse.StrToVarVal("1234", lhproto.VariableType_INT)
	assert.Nil(t, err)
	assert.Equal(t, int64(1234), result.GetInt())

	result, err = littlehorse.StrToVarVal("1234", lhproto.VariableType_STR)
	assert.Nil(t, err)
	assert.Equal(t, "1234", result.GetStr())

	_, err = littlehorse.StrToVarVal("not-an-int", lhproto.VariableType_INT)
	assert.NotNil(t, err)

	result, _ = littlehorse.StrToVarVal("true", lhproto.VariableType_BOOL)
	assert.True(t, result.GetBool())

	result, _ = littlehorse.StrToVarVal("false", lhproto.VariableType_BOOL)
	assert.False(t, result.GetBool())
}

func TestReflectTypeToLHVarType(t *testing.T) {
	testMap := map[reflect.Type]lhproto.VariableType{
		reflect.TypeOf(""):                lhproto.VariableType_STR,
		reflect.TypeOf(time.Time{}):       lhproto.VariableType_TIMESTAMP,
		reflect.TypeOf(lhproto.WfRunId{}): lhproto.VariableType_WF_RUN_ID,
		reflect.TypeOf(1):                 lhproto.VariableType_INT,
		reflect.TypeOf(true):              lhproto.VariableType_BOOL,
		reflect.TypeOf([]int{}):           lhproto.VariableType_JSON_ARR,
		reflect.TypeOf(map[string]int{}):  lhproto.VariableType_JSON_OBJ,
		reflect.TypeOf(struct{}{}):        lhproto.VariableType_JSON_OBJ,
	}

	for key := range testMap {
		result := littlehorse.ReflectTypeToVarType(key)
		assert.Equal(t, testMap[key], result)
	}
}

func primitiveTypeDef(t lhproto.VariableType) *lhproto.TypeDefinition {
	return &lhproto.TypeDefinition{
		DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: t},
	}
}

func arrayTypeDef(elem *lhproto.TypeDefinition) *lhproto.TypeDefinition {
	return &lhproto.TypeDefinition{
		DefinedType: &lhproto.TypeDefinition_InlineArrayDef{
			InlineArrayDef: &lhproto.InlineArrayDef{ArrayType: elem},
		},
	}
}

func mapTypeDef(keyType, valueType *lhproto.TypeDefinition) *lhproto.TypeDefinition {
	return &lhproto.TypeDefinition{
		DefinedType: &lhproto.TypeDefinition_InlineMapDef{
			InlineMapDef: &lhproto.InlineMapDef{KeyType: keyType, ValueType: valueType},
		},
	}
}

func TestTypeDefToVarValPrimitivePassthrough(t *testing.T) {
	result, err := littlehorse.TypeDefToVarVal("hello", primitiveTypeDef(lhproto.VariableType_STR))
	assert.Nil(t, err)
	assert.Equal(t, "hello", result.GetStr())

	result, err = littlehorse.TypeDefToVarVal("42", primitiveTypeDef(lhproto.VariableType_INT))
	assert.Nil(t, err)
	assert.Equal(t, int64(42), result.GetInt())
}

func TestTypeDefToVarValTimestamp(t *testing.T) {
	// ISO-8601 form.
	result, err := littlehorse.TypeDefToVarVal(
		"2026-01-01T00:00:00Z", primitiveTypeDef(lhproto.VariableType_TIMESTAMP),
	)
	assert.Nil(t, err)
	expected := time.Date(2026, 1, 1, 0, 0, 0, 0, time.UTC)
	assert.True(t, result.GetUtcTimestamp().AsTime().Equal(expected))

	// Epoch-millis form.
	result, err = littlehorse.TypeDefToVarVal(
		"1767225600000", primitiveTypeDef(lhproto.VariableType_TIMESTAMP),
	)
	assert.Nil(t, err)
	assert.True(t, result.GetUtcTimestamp().AsTime().Equal(expected))
}

func TestTypeDefToVarValArrayOfInt(t *testing.T) {
	result, err := littlehorse.TypeDefToVarVal(
		"[1, 2, 3]", arrayTypeDef(primitiveTypeDef(lhproto.VariableType_INT)),
	)
	assert.Nil(t, err)

	items := result.GetArray().GetItems()
	assert.Len(t, items, 3)
	assert.Equal(t, int64(1), items[0].GetInt())
	assert.Equal(t, int64(2), items[1].GetInt())
	assert.Equal(t, int64(3), items[2].GetInt())
}

func TestTypeDefToVarValNestedArray(t *testing.T) {
	result, err := littlehorse.TypeDefToVarVal(
		"[[1, 2], [3]]",
		arrayTypeDef(arrayTypeDef(primitiveTypeDef(lhproto.VariableType_INT))),
	)
	assert.Nil(t, err)

	outer := result.GetArray().GetItems()
	assert.Len(t, outer, 2)
	assert.Len(t, outer[0].GetArray().GetItems(), 2)
	assert.Equal(t, int64(3), outer[1].GetArray().GetItems()[0].GetInt())
}

func TestTypeDefToVarValMapStrToInt(t *testing.T) {
	result, err := littlehorse.TypeDefToVarVal(
		`{"hello": 1, "world": 2}`,
		mapTypeDef(primitiveTypeDef(lhproto.VariableType_STR), primitiveTypeDef(lhproto.VariableType_INT)),
	)
	assert.Nil(t, err)

	got := map[string]int64{}
	for _, entry := range result.GetMap().GetEntries() {
		got[entry.GetKey().GetStr()] = entry.GetValue().GetInt()
	}
	assert.Equal(t, map[string]int64{"hello": 1, "world": 2}, got)
}

func TestTypeDefToVarValMapIntKeyCoercion(t *testing.T) {
	result, err := littlehorse.TypeDefToVarVal(
		`{"1": "one", "2": "two"}`,
		mapTypeDef(primitiveTypeDef(lhproto.VariableType_INT), primitiveTypeDef(lhproto.VariableType_STR)),
	)
	assert.Nil(t, err)

	got := map[int64]string{}
	for _, entry := range result.GetMap().GetEntries() {
		got[entry.GetKey().GetInt()] = entry.GetValue().GetStr()
	}
	assert.Equal(t, map[int64]string{1: "one", 2: "two"}, got)
}

func TestTypeDefToVarValMapOfArray(t *testing.T) {
	result, err := littlehorse.TypeDefToVarVal(
		`{"evens": [2, 4], "odds": [1, 3]}`,
		mapTypeDef(
			primitiveTypeDef(lhproto.VariableType_STR),
			arrayTypeDef(primitiveTypeDef(lhproto.VariableType_INT)),
		),
	)
	assert.Nil(t, err)

	got := map[string][]int64{}
	for _, entry := range result.GetMap().GetEntries() {
		var nums []int64
		for _, item := range entry.GetValue().GetArray().GetItems() {
			nums = append(nums, item.GetInt())
		}
		got[entry.GetKey().GetStr()] = nums
	}
	assert.Equal(t, map[string][]int64{"evens": {2, 4}, "odds": {1, 3}}, got)
}

func TestTypeDefToVarValNullMapValue(t *testing.T) {
	result, err := littlehorse.TypeDefToVarVal(
		`{"present": 1, "missing": null}`,
		mapTypeDef(primitiveTypeDef(lhproto.VariableType_STR), primitiveTypeDef(lhproto.VariableType_INT)),
	)
	assert.Nil(t, err)

	entries := result.GetMap().GetEntries()
	assert.Len(t, entries, 2)
	for _, entry := range entries {
		if entry.GetKey().GetStr() == "missing" {
			assert.Nil(t, entry.GetValue().GetValue())
		}
	}
}

func TestTypeDefToVarValWfRunIdWithParent(t *testing.T) {
	result, err := littlehorse.TypeDefToVarVal(
		`{"a": "0a1b2c3d_9f8e7d6c"}`,
		mapTypeDef(primitiveTypeDef(lhproto.VariableType_STR), primitiveTypeDef(lhproto.VariableType_WF_RUN_ID)),
	)
	assert.Nil(t, err)

	entries := result.GetMap().GetEntries()
	assert.Len(t, entries, 1)
	wfRunId := entries[0].GetValue().GetWfRunId()
	assert.Equal(t, "9f8e7d6c", wfRunId.GetId())
	assert.Equal(t, "0a1b2c3d", wfRunId.GetParentWfRunId().GetId())
}

func TestTypeDefToVarValStructValueRejected(t *testing.T) {
	structType := &lhproto.TypeDefinition{
		DefinedType: &lhproto.TypeDefinition_StructDefId{
			StructDefId: &lhproto.StructDefId{Name: "Customer"},
		},
	}
	_, err := littlehorse.TypeDefToVarVal(`{}`, structType)
	assert.NotNil(t, err)
}

func TestTypeDefToVarValMalformedJson(t *testing.T) {
	_, err := littlehorse.TypeDefToVarVal(
		"[1, 2,", arrayTypeDef(primitiveTypeDef(lhproto.VariableType_INT)),
	)
	assert.NotNil(t, err)
}

func TestTypeDefToVarValWrongShape(t *testing.T) {
	// A JSON object provided for an Array type should fail.
	_, err := littlehorse.TypeDefToVarVal(
		`{"a": 1}`, arrayTypeDef(primitiveTypeDef(lhproto.VariableType_INT)),
	)
	assert.NotNil(t, err)
}

func structDefIdTypeDef(name string, version int32) *lhproto.TypeDefinition {
	return &lhproto.TypeDefinition{
		DefinedType: &lhproto.TypeDefinition_StructDefId{
			StructDefId: &lhproto.StructDefId{Name: name, Version: version},
		},
	}
}

func structField(fieldType *lhproto.TypeDefinition) *lhproto.StructFieldDef {
	return &lhproto.StructFieldDef{FieldType: fieldType}
}

// fakeStructDefs is a small in-memory catalog of StructDefs keyed by name, used
// to back the resolver in tests.
func fakeStructDefs() map[string]*lhproto.StructDef {
	address := &lhproto.StructDef{
		Id: &lhproto.StructDefId{Name: "Address", Version: 0},
		StructDef: &lhproto.InlineStructDef{
			Fields: map[string]*lhproto.StructFieldDef{
				"street": structField(primitiveTypeDef(lhproto.VariableType_STR)),
			},
		},
	}
	customer := &lhproto.StructDef{
		Id: &lhproto.StructDefId{Name: "Customer", Version: 0},
		StructDef: &lhproto.InlineStructDef{
			Fields: map[string]*lhproto.StructFieldDef{
				"name":    structField(primitiveTypeDef(lhproto.VariableType_STR)),
				"age":     structField(primitiveTypeDef(lhproto.VariableType_INT)),
				"address": structField(structDefIdTypeDef("Address", 0)),
			},
		},
	}
	return map[string]*lhproto.StructDef{
		"Address":  address,
		"Customer": customer,
	}
}

func fakeResolver(catalog map[string]*lhproto.StructDef) littlehorse.StructDefResolver {
	return func(id *lhproto.StructDefId) (*lhproto.StructDef, error) {
		sd, ok := catalog[id.GetName()]
		if !ok {
			return nil, assert.AnError
		}
		return sd, nil
	}
}

func TestTypeDefToVarValStructNested(t *testing.T) {
	resolver := fakeResolver(fakeStructDefs())
	result, err := littlehorse.TypeDefToVarValWithResolver(
		`{"name": "Joe", "age": 30, "address": {"street": "123 Main"}}`,
		structDefIdTypeDef("Customer", 0),
		resolver,
	)
	assert.Nil(t, err)

	fields := result.GetStruct().GetStruct().GetFields()
	assert.Equal(t, "Joe", fields["name"].GetValue().GetStr())
	assert.Equal(t, int64(30), fields["age"].GetValue().GetInt())
	assert.Equal(t, "Customer", result.GetStruct().GetStructDefId().GetName())

	addr := fields["address"].GetValue().GetStruct().GetStruct().GetFields()
	assert.Equal(t, "123 Main", addr["street"].GetValue().GetStr())
}

func TestTypeDefToVarValArrayOfStruct(t *testing.T) {
	resolver := fakeResolver(fakeStructDefs())
	result, err := littlehorse.TypeDefToVarValWithResolver(
		`[{"street": "1 First"}, {"street": "2 Second"}]`,
		arrayTypeDef(structDefIdTypeDef("Address", 0)),
		resolver,
	)
	assert.Nil(t, err)

	items := result.GetArray().GetItems()
	assert.Len(t, items, 2)
	assert.Equal(t, "1 First", items[0].GetStruct().GetStruct().GetFields()["street"].GetValue().GetStr())
	assert.Equal(t, "2 Second", items[1].GetStruct().GetStruct().GetFields()["street"].GetValue().GetStr())
}

func TestTypeDefToVarValMapOfStruct(t *testing.T) {
	resolver := fakeResolver(fakeStructDefs())
	result, err := littlehorse.TypeDefToVarValWithResolver(
		`{"home": {"street": "1 First"}}`,
		mapTypeDef(primitiveTypeDef(lhproto.VariableType_STR), structDefIdTypeDef("Address", 0)),
		resolver,
	)
	assert.Nil(t, err)

	entries := result.GetMap().GetEntries()
	assert.Len(t, entries, 1)
	assert.Equal(t, "home", entries[0].GetKey().GetStr())
	street := entries[0].GetValue().GetStruct().GetStruct().GetFields()["street"].GetValue().GetStr()
	assert.Equal(t, "1 First", street)
}

func TestTypeDefToVarValStructDefaultFieldOmitted(t *testing.T) {
	catalog := map[string]*lhproto.StructDef{
		"WithDefault": {
			Id: &lhproto.StructDefId{Name: "WithDefault"},
			StructDef: &lhproto.InlineStructDef{
				Fields: map[string]*lhproto.StructFieldDef{
					"required": structField(primitiveTypeDef(lhproto.VariableType_STR)),
					"withDefault": {
						FieldType:    primitiveTypeDef(lhproto.VariableType_INT),
						DefaultValue: &lhproto.VariableValue{Value: &lhproto.VariableValue_Int{Int: 7}},
					},
				},
			},
		},
	}
	result, err := littlehorse.TypeDefToVarValWithResolver(
		`{"required": "hi"}`, structDefIdTypeDef("WithDefault", 0), fakeResolver(catalog),
	)
	assert.Nil(t, err)

	fields := result.GetStruct().GetStruct().GetFields()
	// Field with a default is omitted so the server applies the default.
	_, present := fields["withDefault"]
	assert.False(t, present)
	assert.Equal(t, "hi", fields["required"].GetValue().GetStr())
}

func TestTypeDefToVarValStructNullableNull(t *testing.T) {
	catalog := map[string]*lhproto.StructDef{
		"WithNullable": {
			Id: &lhproto.StructDefId{Name: "WithNullable"},
			StructDef: &lhproto.InlineStructDef{
				Fields: map[string]*lhproto.StructFieldDef{
					"maybe": {
						FieldType:  primitiveTypeDef(lhproto.VariableType_STR),
						IsNullable: true,
					},
				},
			},
		},
	}
	result, err := littlehorse.TypeDefToVarValWithResolver(
		`{"maybe": null}`, structDefIdTypeDef("WithNullable", 0), fakeResolver(catalog),
	)
	assert.Nil(t, err)

	fields := result.GetStruct().GetStruct().GetFields()
	assert.Nil(t, fields["maybe"].GetValue().GetValue())
}

func TestTypeDefToVarValStructMissingRequiredField(t *testing.T) {
	resolver := fakeResolver(fakeStructDefs())
	_, err := littlehorse.TypeDefToVarValWithResolver(
		`{"name": "Joe"}`, structDefIdTypeDef("Customer", 0), resolver,
	)
	assert.NotNil(t, err)
}

func TestTypeDefToVarValStructUnknownField(t *testing.T) {
	resolver := fakeResolver(fakeStructDefs())
	_, err := littlehorse.TypeDefToVarValWithResolver(
		`{"name": "Joe", "age": 30, "address": {"street": "x"}, "extra": true}`,
		structDefIdTypeDef("Customer", 0), resolver,
	)
	assert.NotNil(t, err)
}

func TestTypeDefToVarValStructNotNullableNull(t *testing.T) {
	resolver := fakeResolver(fakeStructDefs())
	_, err := littlehorse.TypeDefToVarValWithResolver(
		`{"name": null, "age": 30, "address": {"street": "x"}}`,
		structDefIdTypeDef("Customer", 0), resolver,
	)
	assert.NotNil(t, err)
}

func TestTypeDefToVarValStructNoResolver(t *testing.T) {
	// Without a resolver, a StructDefId-typed value cannot be coerced.
	_, err := littlehorse.TypeDefToVarValWithResolver(
		`{"street": "x"}`, structDefIdTypeDef("Address", 0), nil,
	)
	assert.NotNil(t, err)
}
