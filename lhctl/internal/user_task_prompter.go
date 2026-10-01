package internal

import (
	"bufio"
	"errors"
	"fmt"
	"io"
	"sort"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

type userTaskPrompter struct {
	reader   *bufio.Reader
	writer   io.Writer
	resolver littlehorse.StructDefResolver
}

func (p *userTaskPrompter) read(prompt string) (string, error) {
	if _, err := fmt.Fprint(p.writer, prompt+": "); err != nil {
		return "", err
	}
	line, err := p.reader.ReadString('\n')
	if errors.Is(err, io.EOF) && line != "" {
		err = nil
	}
	return strings.TrimSpace(line), err
}

func (p *userTaskPrompter) value(name string, typeDef *lhproto.TypeDefinition) (*lhproto.VariableValue, error) {
	if typeDef == nil {
		return nil, fmt.Errorf("missing type for %s", name)
	}

	switch def := typeDef.GetDefinedType().(type) {
	case *lhproto.TypeDefinition_StructDefId:
		if p.resolver == nil {
			return nil, fmt.Errorf("cannot resolve StructDef for %s", name)
		}
		structDef, err := p.resolver(def.StructDefId)
		if err != nil {
			return nil, fmt.Errorf("fetching StructDef for %s: %w", name, err)
		}
		if structDef == nil || structDef.GetStructDef() == nil {
			return nil, fmt.Errorf("StructDef for %s has no schema", name)
		}
		return p.structValue(name, structDef.GetStructDef())
	case *lhproto.TypeDefinition_InlineStructDef:
		return p.structValue(name, def.InlineStructDef)
	default:
		label := name
		if label == "" {
			label = "Value"
		}
		kind := "JSON"
		if primitive, ok := def.(*lhproto.TypeDefinition_PrimitiveType); ok {
			kind = primitive.PrimitiveType.String()
		}
		input, err := p.read(fmt.Sprintf("%s (%s)", label, kind))
		if err != nil {
			return nil, err
		}
		value, err := littlehorse.TypeDefToVarValWithResolver(input, typeDef, p.resolver)
		if err != nil {
			return nil, fmt.Errorf("invalid %s: %w", label, err)
		}
		return value, nil
	}
}

func (p *userTaskPrompter) structValue(name string, def *lhproto.InlineStructDef) (*lhproto.VariableValue, error) {
	fields := make(map[string]*lhproto.StructField, len(def.GetFields()))
	names := make([]string, 0, len(def.GetFields()))
	for fieldName := range def.GetFields() {
		names = append(names, fieldName)
	}
	sort.Strings(names)

	for _, fieldName := range names {
		fieldDef := def.GetFields()[fieldName]
		path := fieldName
		if name != "" {
			path = name + "." + fieldName
		}
		if fieldDef.GetDescription() != "" {
			if _, err := fmt.Fprintln(p.writer, fieldDef.GetDescription()); err != nil {
				return nil, err
			}
		}
		if fieldDef.DefaultValue != nil || fieldDef.GetIsNullable() {
			answer, err := p.read("Include " + path + "? [y/N]")
			if err != nil {
				return nil, err
			}
			if !strings.EqualFold(answer, "y") && !strings.EqualFold(answer, "yes") {
				continue
			}
		}
		value, err := p.value(path, fieldDef.GetFieldType())
		if err != nil {
			return nil, err
		}
		fields[fieldName] = &lhproto.StructField{Value: value}
	}

	return &lhproto.VariableValue{Value: &lhproto.VariableValue_Struct{
		Struct: &lhproto.Struct{Struct: &lhproto.InlineStruct{Fields: fields}},
	}}, nil
}
