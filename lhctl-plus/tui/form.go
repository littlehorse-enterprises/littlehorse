package tui

import (
	"context"
	"fmt"
	"strings"

	"github.com/charmbracelet/bubbles/textinput"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/jhump/protoreflect/desc"
	"github.com/jhump/protoreflect/dynamic"
	"github.com/jhump/protoreflect/dynamic/grpcdynamic"
	"github.com/littlehorse-enterprises/lhctl-plus/reflection"
	"google.golang.org/grpc"
	dpb "google.golang.org/protobuf/types/descriptorpb"
)

// formField describes a single form input.
type formField struct {
	fieldDesc *desc.FieldDescriptor
	label     string
	typeHint  string
	input     textinput.Model
	required  bool
	isEnum    bool
	enumVals  []string
	isBool    bool
}

// formModel handles the RPC input form.
type formModel struct {
	rpc    reflection.RPCInfo
	fields []formField
	cursor int
	width  int
	height int
	conn   *grpc.ClientConn
	err    string
	offset int // scroll offset for long forms
}

func newFormModel(rpc reflection.RPCInfo, conn *grpc.ClientConn) formModel {
	fields := buildFields(rpc.Input)
	if len(fields) > 0 {
		fields[0].input.Focus()
	}
	return formModel{
		rpc:    rpc,
		fields: fields,
		conn:   conn,
	}
}

func (f *formModel) setSize(w, h int) {
	f.width = w
	f.height = h
	for i := range f.fields {
		f.fields[i].input.Width = w - 6
	}
}

func buildFields(msgDesc *desc.MessageDescriptor) []formField {
	if msgDesc == nil {
		return nil
	}

	var fields []formField
	for _, fd := range msgDesc.GetFields() {
		field := formField{
			fieldDesc: fd,
			label:     fd.GetName(),
			typeHint:  fieldTypeLabel(fd),
		}

		ti := textinput.New()
		ti.Placeholder = field.typeHint
		ti.CharLimit = 500

		switch fd.GetType() {
		case dpb.FieldDescriptorProto_TYPE_BOOL:
			field.isBool = true
			ti.Placeholder = "true / false"
		case dpb.FieldDescriptorProto_TYPE_ENUM:
			field.isEnum = true
			var vals []string
			for _, v := range fd.GetEnumType().GetValues() {
				vals = append(vals, v.GetName())
			}
			field.enumVals = vals
			ti.Placeholder = strings.Join(vals, " | ")
		case dpb.FieldDescriptorProto_TYPE_MESSAGE:
			ti.Placeholder = fmt.Sprintf("{json: %s}", fd.GetMessageType().GetName())
		}

		if fd.IsRequired() {
			field.required = true
		}

		field.input = ti
		fields = append(fields, field)
	}
	return fields
}

func fieldTypeLabel(fd *desc.FieldDescriptor) string {
	typeName := fd.GetType().String()
	typeName = strings.TrimPrefix(typeName, "TYPE_")
	typeName = strings.ToLower(typeName)

	if fd.GetType() == dpb.FieldDescriptorProto_TYPE_MESSAGE {
		typeName = fd.GetMessageType().GetName()
	}
	if fd.GetType() == dpb.FieldDescriptorProto_TYPE_ENUM {
		typeName = fd.GetEnumType().GetName()
	}
	if fd.IsRepeated() {
		typeName = "repeated " + typeName
	}
	if fd.IsMap() {
		typeName = "map"
	}

	label := fd.GetLabel().String()
	if label == "LABEL_OPTIONAL" {
		return typeName + " (optional)"
	}
	return typeName
}

func (f formModel) update(msg tea.Msg) (formModel, tea.Cmd) {
	if len(f.fields) == 0 {
		// No fields — immediately invoke the RPC
		switch msg := msg.(type) {
		case tea.KeyMsg:
			if msg.String() == "enter" {
				return f, f.submit()
			}
		}
		return f, nil
	}

	switch msg := msg.(type) {
	case tea.KeyMsg:
		switch msg.String() {
		case "tab", "down":
			f.fields[f.cursor].input.Blur()
			f.cursor = (f.cursor + 1) % len(f.fields)
			f.fields[f.cursor].input.Focus()
			f.ensureVisible()
			return f, nil
		case "shift+tab", "up":
			f.fields[f.cursor].input.Blur()
			f.cursor = (f.cursor - 1 + len(f.fields)) % len(f.fields)
			f.fields[f.cursor].input.Focus()
			f.ensureVisible()
			return f, nil
		case "enter":
			return f, f.submit()
		}
	}

	var cmd tea.Cmd
	f.fields[f.cursor].input, cmd = f.fields[f.cursor].input.Update(msg)
	return f, cmd
}

func (f *formModel) ensureVisible() {
	visible := f.visibleFields()
	if f.cursor < f.offset {
		f.offset = f.cursor
	} else if f.cursor >= f.offset+visible {
		f.offset = f.cursor - visible + 1
	}
}

func (f formModel) visibleFields() int {
	// header takes ~4 lines, each field takes ~2 lines
	rows := (f.height - 5) / 2
	if rows < 1 {
		rows = 1
	}
	return rows
}

func (f formModel) view() string {
	var s strings.Builder

	header := titleStyle.Render(fmt.Sprintf("⚡ %s", f.rpc.MethodName))
	s.WriteString(header)
	s.WriteString("\n")

	if f.rpc.Description != "" {
		s.WriteString(descriptionStyle.Render(f.rpc.Description))
		s.WriteString("\n")
	}
	s.WriteString("\n")

	if len(f.fields) == 0 {
		s.WriteString(descriptionStyle.Render("  This RPC takes no parameters. Press Enter to execute."))
		s.WriteString("\n")
	} else {
		visible := f.visibleFields()
		end := f.offset + visible
		if end > len(f.fields) {
			end = len(f.fields)
		}

		for i := f.offset; i < end; i++ {
			field := f.fields[i]
			cursor := "  "
			if i == f.cursor {
				cursor = "▸ "
			}

			label := fieldLabelStyle.Render(field.label)
			typeInfo := fieldTypeStyle.Render(fmt.Sprintf(" (%s)", field.typeHint))

			s.WriteString(fmt.Sprintf("%s%s%s\n", cursor, label, typeInfo))
			s.WriteString(fmt.Sprintf("    %s\n", field.input.View()))
		}

		if len(f.fields) > visible {
			s.WriteString(helpStyle.Render(fmt.Sprintf("\n  field %d/%d", f.cursor+1, len(f.fields))))
			s.WriteString("\n")
		}
	}

	if f.err != "" {
		s.WriteString("\n")
		s.WriteString(errorStyle.Render("  Error: " + f.err))
		s.WriteString("\n")
	}

	return s.String()
}

func (f formModel) submit() tea.Cmd {
	return func() tea.Msg {
		msg := dynamic.NewMessage(f.rpc.Input)

		for _, field := range f.fields {
			val := strings.TrimSpace(field.input.Value())
			if val == "" {
				continue
			}

			fd := field.fieldDesc
			parsed, err := parseFieldValue(fd, val)
			if err != nil {
				return rpcErrorMsg{err: fmt.Errorf("field %s: %w", fd.GetName(), err)}
			}
			msg.SetFieldByNumber(int(fd.GetNumber()), parsed)
		}

		stub := grpcdynamic.NewStub(f.conn)
		resp, err := stub.InvokeRpc(context.Background(), f.rpc.Method, msg)
		if err != nil {
			return rpcResponseMsg{
				json:    fmt.Sprintf("RPC %s failed: %v", f.rpc.MethodName, err),
				isError: true,
			}
		}

		jsonBytes, err := resp.(*dynamic.Message).MarshalJSON()
		if err != nil {
			return rpcResponseMsg{
				json:    fmt.Sprintf("Failed to marshal RPC response: %v", err),
				isError: true,
			}
		}

		return rpcResponseMsg{json: string(jsonBytes), isError: false}
	}
}

// parseFieldValue converts a string input to the appropriate proto field value.
func parseFieldValue(fd *desc.FieldDescriptor, val string) (interface{}, error) {
	switch fd.GetType() {
	case dpb.FieldDescriptorProto_TYPE_STRING:
		return val, nil
	case dpb.FieldDescriptorProto_TYPE_INT32, dpb.FieldDescriptorProto_TYPE_SINT32, dpb.FieldDescriptorProto_TYPE_SFIXED32:
		var v int32
		if _, err := fmt.Sscanf(val, "%d", &v); err != nil {
			return nil, fmt.Errorf("expected int32: %w", err)
		}
		return v, nil
	case dpb.FieldDescriptorProto_TYPE_INT64, dpb.FieldDescriptorProto_TYPE_SINT64, dpb.FieldDescriptorProto_TYPE_SFIXED64:
		var v int64
		if _, err := fmt.Sscanf(val, "%d", &v); err != nil {
			return nil, fmt.Errorf("expected int64: %w", err)
		}
		return v, nil
	case dpb.FieldDescriptorProto_TYPE_UINT32, dpb.FieldDescriptorProto_TYPE_FIXED32:
		var v uint32
		if _, err := fmt.Sscanf(val, "%d", &v); err != nil {
			return nil, fmt.Errorf("expected uint32: %w", err)
		}
		return v, nil
	case dpb.FieldDescriptorProto_TYPE_UINT64, dpb.FieldDescriptorProto_TYPE_FIXED64:
		var v uint64
		if _, err := fmt.Sscanf(val, "%d", &v); err != nil {
			return nil, fmt.Errorf("expected uint64: %w", err)
		}
		return v, nil
	case dpb.FieldDescriptorProto_TYPE_FLOAT:
		var v float32
		if _, err := fmt.Sscanf(val, "%f", &v); err != nil {
			return nil, fmt.Errorf("expected float: %w", err)
		}
		return v, nil
	case dpb.FieldDescriptorProto_TYPE_DOUBLE:
		var v float64
		if _, err := fmt.Sscanf(val, "%f", &v); err != nil {
			return nil, fmt.Errorf("expected double: %w", err)
		}
		return v, nil
	case dpb.FieldDescriptorProto_TYPE_BOOL:
		lower := strings.ToLower(val)
		if lower == "true" || lower == "1" || lower == "yes" {
			return true, nil
		}
		return false, nil
	case dpb.FieldDescriptorProto_TYPE_ENUM:
		for _, ev := range fd.GetEnumType().GetValues() {
			if strings.EqualFold(ev.GetName(), val) {
				return ev.GetNumber(), nil
			}
		}
		return nil, fmt.Errorf("unknown enum value %q", val)
	case dpb.FieldDescriptorProto_TYPE_MESSAGE:
		nested := dynamic.NewMessage(fd.GetMessageType())
		if err := nested.UnmarshalJSON([]byte(val)); err != nil {
			return nil, fmt.Errorf("invalid JSON for %s: %w", fd.GetMessageType().GetName(), err)
		}
		return nested, nil
	case dpb.FieldDescriptorProto_TYPE_BYTES:
		return []byte(val), nil
	default:
		return val, nil
	}
}
