package internal

import (
	"fmt"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func TestPartialStructResultsFromJSON(t *testing.T) {
	addressId := &lhproto.StructDefId{Name: "address", Version: 0}
	address := &lhproto.StructDef{
		Id: addressId,
		StructDef: &lhproto.InlineStructDef{Fields: map[string]*lhproto.StructFieldDef{
			"city": {FieldType: primitiveType(lhproto.VariableType_STR)},
		}},
	}
	approval := &lhproto.StructDef{
		Id: &lhproto.StructDefId{Name: "approval", Version: 0},
		StructDef: &lhproto.InlineStructDef{Fields: map[string]*lhproto.StructFieldDef{
			"approved": {FieldType: primitiveType(lhproto.VariableType_BOOL)},
			"address":  {FieldType: structType(addressId)},
		}},
	}
	resolver := func(id *lhproto.StructDefId) (*lhproto.StructDef, error) {
		if id.GetName() == addressId.GetName() && id.GetVersion() == addressId.GetVersion() {
			return address, nil
		}
		return nil, fmt.Errorf("unknown StructDef")
	}

	results, err := partialStructResultsFromJSON(
		[]byte(`{"address":{"city":"Denver"}}`), approval, littlehorse.StructDefResolver(resolver),
	)
	if err != nil {
		t.Fatalf("unexpected error: %v", err)
	}
	if len(results) != 1 {
		t.Fatalf("expected one partial result, got %d", len(results))
	}
	if got := results["address"].GetStruct().GetStruct().GetFields()["city"].GetValue().GetStr(); got != "Denver" {
		t.Fatalf("expected nested city Denver, got %q", got)
	}
}

func TestPartialStructResultsFromJSONRejectsUnknownField(t *testing.T) {
	definition := &lhproto.StructDef{
		Id:        &lhproto.StructDefId{Name: "approval", Version: 0},
		StructDef: &lhproto.InlineStructDef{Fields: map[string]*lhproto.StructFieldDef{}},
	}

	_, err := partialStructResultsFromJSON([]byte(`{"unknown":true}`), definition, nil)
	if err == nil {
		t.Fatal("expected unknown field error")
	}
}

func primitiveType(variableType lhproto.VariableType) *lhproto.TypeDefinition {
	return &lhproto.TypeDefinition{
		DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: variableType},
	}
}

func structType(id *lhproto.StructDefId) *lhproto.TypeDefinition {
	return &lhproto.TypeDefinition{
		DefinedType: &lhproto.TypeDefinition_StructDefId{StructDefId: id},
	}
}
