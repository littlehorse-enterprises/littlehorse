package tui

import (
	"context"
	"fmt"
	"strings"
	"time"

	"encoding/json"

	"github.com/charmbracelet/bubbles/textinput"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/jhump/protoreflect/desc"
	"github.com/jhump/protoreflect/dynamic"
	"github.com/jhump/protoreflect/dynamic/grpcdynamic"
	"github.com/littlehorse-enterprises/lhctl-plus/reflection"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"google.golang.org/grpc"
	"google.golang.org/protobuf/encoding/protojson"
	dpb "google.golang.org/protobuf/types/descriptorpb"
	timestamppb "google.golang.org/protobuf/types/known/timestamppb"
)

type wfSpecSuggestionsMsg struct {
	fieldIndex  int
	query       string
	suggestions []string
	err         error
}

type wfSpecDebounceMsg struct {
	fieldIndex int
	query      string
	seq        int
}

type wfSpecFetchedMsg struct {
	fieldIndex int
	query      string
	spec       *lhproto.WfSpec
	err        error
}

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

	// WfSpec variable specific
	isWfSpecVar bool
	varName     string
	varTypeCase string // e.g. "str", "int", "jsonObj", etc.
	isSection   bool

	suggestionOpen    bool
	suggestionLoading bool
	suggestionError   string
	suggestionValues  []string
	suggestionCursor  int
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

	wfSpecSuggestionCache map[string][]string
	wfSpecDebounceSeq     int
}

func newFormModel(rpc reflection.RPCInfo, conn *grpc.ClientConn) formModel {
	fields := buildFields(rpc.Input)
	// For RunWf, hide the raw `variables` map field (we'll surface individual inputs).
	if rpc.MethodName == "RunWf" {
		var kept []formField
		for _, f := range fields {
			if f.fieldDesc != nil && f.fieldDesc.GetName() == "variables" {
				continue
			}
			kept = append(kept, f)
		}
		fields = kept
	}

	// focus the first focusable input
	if len(fields) > 0 {
		// find first non-section
		for i := range fields {
			if !fields[i].isSection {
				fields[i].input.Focus()
				break
			}
		}
	}
	return formModel{
		rpc:                   rpc,
		fields:                fields,
		conn:                  conn,
		wfSpecSuggestionCache: make(map[string][]string),
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
	switch msg := msg.(type) {
	case wfSpecSuggestionsMsg:
		if msg.fieldIndex < 0 || msg.fieldIndex >= len(f.fields) {
			return f, nil
		}
		field := &f.fields[msg.fieldIndex]
		if !f.isWfSpecNameSmartField(msg.fieldIndex) {
			return f, nil
		}

		field.suggestionLoading = false
		field.suggestionError = ""
		field.suggestionOpen = true
		field.suggestionCursor = 0
		field.suggestionValues = msg.suggestions

		if msg.err != nil {
			field.suggestionError = msg.err.Error()
			field.suggestionValues = nil
			return f, nil
		}

		cacheKey := strings.ToLower(strings.TrimSpace(msg.query))
		if cacheKey != "" {
			f.wfSpecSuggestionCache[cacheKey] = msg.suggestions
		}

		// If suggestions include an exact match for the query, proactively fetch the WfSpec
		for _, s := range msg.suggestions {
			if strings.EqualFold(strings.TrimSpace(s), strings.TrimSpace(msg.query)) {
				return f, f.fetchWfSpec(msg.fieldIndex, s)
			}
		}
		return f, nil

	case wfSpecDebounceMsg:
		if msg.seq != f.wfSpecDebounceSeq {
			return f, nil
		}
		if msg.fieldIndex < 0 || msg.fieldIndex >= len(f.fields) {
			return f, nil
		}
		if !f.isWfSpecNameSmartField(msg.fieldIndex) {
			return f, nil
		}

		currentQuery := strings.TrimSpace(f.fields[msg.fieldIndex].input.Value())
		if currentQuery == "" || !strings.EqualFold(currentQuery, msg.query) {
			return f, nil
		}

		cacheKey := strings.ToLower(currentQuery)
		if cached, ok := f.wfSpecSuggestionCache[cacheKey]; ok {
			field := &f.fields[msg.fieldIndex]
			field.suggestionOpen = true
			field.suggestionLoading = false
			field.suggestionError = ""
			field.suggestionValues = cached
			field.suggestionCursor = 0
			return f, nil
		}

		field := &f.fields[msg.fieldIndex]
		field.suggestionLoading = true
		field.suggestionOpen = true
		field.suggestionError = ""
		// keep existing suggestionValues visible until new results arrive
		return f, f.fetchWfSpecSuggestions(msg.fieldIndex, currentQuery)

	case wfSpecFetchedMsg:
		if msg.fieldIndex < 0 || msg.fieldIndex >= len(f.fields) {
			return f, nil
		}
		field := &f.fields[msg.fieldIndex]
		field.suggestionLoading = false
		if msg.err != nil {
			field.suggestionError = msg.err.Error()
			return f, nil
		}
		// Insert variable input fields derived from the WfSpec
		f.insertWfSpecVariables(msg.fieldIndex, msg.spec)
		return f, nil
	}

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
		if f.isWfSpecNameSmartField(f.cursor) {
			field := &f.fields[f.cursor]
			switch msg.String() {
			case "up":
				if field.suggestionOpen && len(field.suggestionValues) > 0 {
					if field.suggestionCursor > 0 {
						field.suggestionCursor--
					}
					return f, nil
				}
				// otherwise fallthrough to navigation (handled below)
			case "down":
				if field.suggestionOpen && len(field.suggestionValues) > 0 {
					if field.suggestionCursor < len(field.suggestionValues)-1 {
						field.suggestionCursor++
					}
					return f, nil
				}
				// otherwise fallthrough to navigation (handled below)
			case "esc":
				if field.suggestionOpen {
					f.closeSuggestions(f.cursor)
					return f, nil
				}
			case "enter":
				if field.suggestionOpen {
					if len(field.suggestionValues) > 0 {
						val := field.suggestionValues[field.suggestionCursor]
						field.input.SetValue(val)
						// fetch full WfSpec for this exact selection
						f.closeSuggestions(f.cursor)
						return f, f.fetchWfSpec(f.cursor, val)
					}
					f.closeSuggestions(f.cursor)
					return f, nil
				}
				// When suggestion list is not active, Enter keeps normal submit behavior.
			}
		}

		switch msg.String() {
		case "tab", "down":
			f.closeSuggestions(f.cursor)
			f.fields[f.cursor].input.Blur()
			f.cursor = f.nextFocusableIndex(f.cursor, 1)
			f.fields[f.cursor].input.Focus()
			f.ensureVisible()
			return f, nil
		case "shift+tab", "up":
			f.closeSuggestions(f.cursor)
			f.fields[f.cursor].input.Blur()
			f.cursor = f.nextFocusableIndex(f.cursor, -1)
			f.fields[f.cursor].input.Focus()
			f.ensureVisible()
			return f, nil
		case "enter":
			return f, f.submit()
		}
	}

	var cmd tea.Cmd
	fieldBefore := f.fields[f.cursor].input.Value()
	f.fields[f.cursor].input, cmd = f.fields[f.cursor].input.Update(msg)
	if f.isWfSpecNameSmartField(f.cursor) {
		// Only invalidate suggestions if the actual text changed.
		fieldAfter := strings.TrimSpace(f.fields[f.cursor].input.Value())
		if strings.TrimSpace(fieldBefore) != fieldAfter {
			if fieldAfter == "" {
				// if user cleared input, close suggestions
				f.closeSuggestions(f.cursor)
				return f, cmd
			}

			// keep existing suggestions visible while debounce timer runs
			f.wfSpecDebounceSeq++
			debounceCmd := f.scheduleWfSpecDebounce(f.cursor, fieldAfter, f.wfSpecDebounceSeq)
			if cmd != nil {
				return f, tea.Batch(cmd, debounceCmd)
			}
			return f, debounceCmd
		}
	}
	return f, cmd
}

func (f formModel) scheduleWfSpecDebounce(fieldIndex int, query string, seq int) tea.Cmd {
	return tea.Tick(250*time.Millisecond, func(_ time.Time) tea.Msg {
		return wfSpecDebounceMsg{fieldIndex: fieldIndex, query: query, seq: seq}
	})
}

func (f formModel) fetchWfSpecSuggestions(fieldIndex int, query string) tea.Cmd {
	return func() tea.Msg {
		client := lhproto.NewLittleHorseClient(f.conn)
		limit := int32(10)
		resp, err := client.SearchWfSpec(context.Background(), &lhproto.SearchWfSpecRequest{
			Limit: &limit,
			WfSpecCriteria: &lhproto.SearchWfSpecRequest_Prefix{
				Prefix: query,
			},
		})
		if err != nil {
			return wfSpecSuggestionsMsg{fieldIndex: fieldIndex, query: query, err: err}
		}

		names := make([]string, 0, len(resp.GetResults()))
		seen := make(map[string]struct{})
		for _, id := range resp.GetResults() {
			if id == nil || id.GetName() == "" {
				continue
			}
			if _, ok := seen[id.GetName()]; ok {
				continue
			}
			seen[id.GetName()] = struct{}{}
			names = append(names, id.GetName())
		}
		return wfSpecSuggestionsMsg{fieldIndex: fieldIndex, query: query, suggestions: names}
	}
}

func (f formModel) fetchWfSpec(fieldIndex int, name string) tea.Cmd {
	return func() tea.Msg {
		client := lhproto.NewLittleHorseClient(f.conn)
		// Use only name; leave version/revision unset (server will return latest)
		resp, err := client.GetWfSpec(context.Background(), &lhproto.WfSpecId{Name: name})
		if err != nil {
			return wfSpecFetchedMsg{fieldIndex: fieldIndex, query: name, err: err}
		}
		return wfSpecFetchedMsg{fieldIndex: fieldIndex, query: name, spec: resp}
	}
}

func (f *formModel) removeExistingWfSpecVars() {
	var kept []formField
	for _, fld := range f.fields {
		if fld.isWfSpecVar {
			continue
		}
		// also remove old Variables section header
		if fld.isSection && strings.EqualFold(fld.label, "Variables") {
			continue
		}
		kept = append(kept, fld)
	}
	f.fields = kept
}

func (f *formModel) insertWfSpecVariables(afterIndex int, spec *lhproto.WfSpec) {
	if spec == nil {
		return
	}
	// remove old wf spec vars first
	f.removeExistingWfSpecVars()

	entry := spec.GetEntrypointThreadName()
	if entry == "" {
		return
	}
	thread := spec.GetThreadSpecs()[entry]
	if thread == nil {
		return
	}

	var varFields []formField
	for _, tv := range thread.GetVariableDefs() {
		vd := tv.GetVarDef()
		if vd == nil {
			continue
		}
		// Skip inherited vars (must be provided by parent wf run)
		if tv.GetAccessLevel() == lhproto.WfRunVariableAccessLevel_INHERITED_VAR {
			continue
		}

		ff := formField{}
		ti := textinput.New()
		// label is variable name
		ff.label = vd.GetName()
		ff.isWfSpecVar = true
		ff.varName = vd.GetName()

		// determine typeCase
		if td := vd.GetTypeDef(); td != nil {
			if td.GetStructDefId() != nil {
				ff.varTypeCase = "struct"
				ti.Placeholder = "{json struct}"
			} else {
				switch td.GetPrimitiveType() {
				case lhproto.VariableType_BOOL:
					ff.varTypeCase = "bool"
					ti.Placeholder = "true / false"
				case lhproto.VariableType_DOUBLE:
					ff.varTypeCase = "double"
					ti.Placeholder = "float"
				case lhproto.VariableType_INT:
					ff.varTypeCase = "int"
					ti.Placeholder = "integer"
				case lhproto.VariableType_STR:
					ff.varTypeCase = "str"
					ti.Placeholder = "string"
				case lhproto.VariableType_JSON_OBJ:
					ff.varTypeCase = "jsonObj"
					ti.Placeholder = "{json}"
				case lhproto.VariableType_JSON_ARR:
					ff.varTypeCase = "jsonArr"
					ti.Placeholder = "[json]"
				case lhproto.VariableType_WF_RUN_ID:
					ff.varTypeCase = "wfRunId"
					ti.Placeholder = "{wfRunId JSON}"
				case lhproto.VariableType_BYTES:
					ff.varTypeCase = "bytes"
					ti.Placeholder = "bytes"
				case lhproto.VariableType_TIMESTAMP:
					ff.varTypeCase = "utcTimestamp"
					ti.Placeholder = "RFC3339 timestamp"
				default:
					ff.varTypeCase = "str"
					ti.Placeholder = "string"
				}
			}
		} else {
			// fallback to deprecated Type
			switch vd.GetType() {
			case lhproto.VariableType_BOOL:
				ff.varTypeCase = "bool"
				ti.Placeholder = "true / false"
			default:
				ff.varTypeCase = "str"
				ti.Placeholder = "string"
			}
		}

		ff.input = ti
		varFields = append(varFields, ff)
	}
	// create a section header
	section := formField{isSection: true, label: "Variables"}

	// Find insertion index: after the main RunWf properties we want to keep together.
	topNames := map[string]struct{}{"wf_spec_name": {}, "major_version": {}, "revision": {}, "id": {}, "parent_wf_run_id": {}}
	insertAt := -1
	for idx, fld := range f.fields {
		if fld.fieldDesc != nil {
			if _, ok := topNames[fld.fieldDesc.GetName()]; ok {
				if idx > insertAt {
					insertAt = idx
				}
			}
		}
	}

	// if none found, append at end
	if insertAt < 0 || insertAt >= len(f.fields) {
		f.fields = append(f.fields, section)
		f.fields = append(f.fields, varFields...)
		return
	}

	head := append([]formField{}, f.fields[:insertAt+1]...)
	tail := append([]formField{}, f.fields[insertAt+1:]...)
	head = append(head, section)
	head = append(head, varFields...)
	head = append(head, tail...)
	f.fields = head
}

func (f formModel) isWfSpecNameSmartField(fieldIndex int) bool {
	if f.rpc.MethodName != "RunWf" {
		return false
	}
	if fieldIndex < 0 || fieldIndex >= len(f.fields) {
		return false
	}
	return f.fields[fieldIndex].fieldDesc != nil && f.fields[fieldIndex].fieldDesc.GetName() == "wf_spec_name"
}

func (f *formModel) closeSuggestions(fieldIndex int) {
	if fieldIndex < 0 || fieldIndex >= len(f.fields) {
		return
	}
	field := &f.fields[fieldIndex]
	field.suggestionOpen = false
	field.suggestionLoading = false
	field.suggestionError = ""
	field.suggestionValues = nil
	field.suggestionCursor = 0
}

func (f *formModel) ensureVisible() {
	visible := f.visibleFields()
	if f.cursor < f.offset {
		f.offset = f.cursor
	} else if f.cursor >= f.offset+visible {
		f.offset = f.cursor - visible + 1
	}
}

// nextFocusableIndex returns the next index from start (inclusive) moving by dir (1 or -1)
// that is not a section. Wraps around.
func (f *formModel) nextFocusableIndex(start, dir int) int {
	if len(f.fields) == 0 {
		return 0
	}
	n := len(f.fields)
	i := start
	for {
		i = (i + dir + n) % n
		if !f.fields[i].isSection {
			return i
		}
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
			// Render section headers differently and skip input rendering
			if field.isSection {
				s.WriteString(categoryStyle.Render(field.label))
				s.WriteString("\n")
				continue
			}

			cursor := "  "
			if i == f.cursor {
				cursor = "▸ "
			}

			label := fieldLabelStyle.Render(field.label)
			typeInfo := fieldTypeStyle.Render(fmt.Sprintf(" (%s)", field.typeHint))

			s.WriteString(fmt.Sprintf("%s%s%s\n", cursor, label, typeInfo))
			// Render the input. When empty, render our placeholderStyle explicitly
			// to control placeholder color instead of embedding ANSI sequences
			// inside the textinput placeholder string.
			inputContent := ""
			if strings.TrimSpace(field.input.Value()) == "" {
				inputContent = placeholderStyle.Render(field.input.Placeholder)
			} else {
				inputContent = field.input.View()
			}

			// When this field is focused, render the input inside a visible box
			// so the user can clearly see which control is active even when
			// the field is empty.
			// Only show a visible highlight when the text input actually has
			// keyboard focus. This prevents the preview pane from showing a
			// highlighted input while the user is navigating the RPC list.
			if i == f.cursor && field.input.Focused() {
				s.WriteString("    ")
				s.WriteString(focusedInputStyle.Render(inputContent))
				s.WriteString("\n")
			} else {
				s.WriteString("    ")
				s.WriteString(inputContent)
				s.WriteString("\n")
			}

			if i == f.cursor && f.isWfSpecNameSmartField(i) && field.suggestionOpen {
				if field.suggestionLoading {
					s.WriteString("      ")
					s.WriteString(helpStyle.Render("Loading wf specs..."))
					s.WriteString("\n")
				} else if field.suggestionError != "" {
					s.WriteString("      ")
					s.WriteString(errorStyle.Render(field.suggestionError))
					s.WriteString("\n")
				} else if len(field.suggestionValues) == 0 {
					s.WriteString("      ")
					s.WriteString(helpStyle.Render("No matching WfSpecs (keep typing)."))
					s.WriteString("\n")
				} else {
					for j, suggestion := range field.suggestionValues {
						prefix := "        "
						lineStyle := helpStyle
						if j == field.suggestionCursor {
							prefix = "      ▶ "
							lineStyle = fieldLabelStyle
						}
						s.WriteString(lineStyle.Render(prefix + suggestion))
						s.WriteString("\n")
					}
				}
			}
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
		// Special-case RunWf to construct a typed request (easier to populate variables map)
		if f.rpc.MethodName == "RunWf" {
			req := &lhproto.RunWfRequest{}
			for _, field := range f.fields {
				val := strings.TrimSpace(field.input.Value())
				if val == "" {
					continue
				}

				if field.isWfSpecVar {
					vv, err := parseToProtoVariableValue(field.varTypeCase, val)
					if err != nil {
						return rpcErrorMsg{err: fmt.Errorf("variable %s: %w", field.varName, err)}
					}
					if req.Variables == nil {
						req.Variables = make(map[string]*lhproto.VariableValue)
					}
					req.Variables[field.varName] = vv
					continue
				}

				// handle known RunWfRequest top-level fields by proto name
				switch field.fieldDesc.GetName() {
				case "wf_spec_name":
					req.WfSpecName = val
				case "major_version":
					var v int32
					fmt.Sscanf(val, "%d", &v)
					req.MajorVersion = &v
				case "revision":
					var v int32
					fmt.Sscanf(val, "%d", &v)
					req.Revision = &v
				case "id":
					req.Id = &val
				case "parent_wf_run_id":
					// accept either JSON or plain id string
					wr := &lhproto.WfRunId{}
					if strings.HasPrefix(val, "{") {
						if err := protojson.Unmarshal([]byte(val), wr); err != nil {
							return rpcErrorMsg{err: fmt.Errorf("invalid parent_wf_run_id JSON: %w", err)}
						}
					} else {
						wr.Id = val
					}
					req.ParentWfRunId = wr
				default:
					// Unknown top-level field; ignore
				}
			}

			client := lhproto.NewLittleHorseClient(f.conn)
			resp, err := client.RunWf(context.Background(), req)
			if err != nil {
				return rpcResponseMsg{json: fmt.Sprintf("RPC RunWf failed: %v", err), isError: true}
			}
			jb, err := protojson.Marshal(resp)
			if err != nil {
				return rpcResponseMsg{json: fmt.Sprintf("marshal response failed: %v", err), isError: true}
			}
			return rpcResponseMsg{json: string(jb), isError: false}
		}

		// Fallback: dynamic invocation for other RPCs
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

func parseToProtoVariableValue(caseName, val string) (*lhproto.VariableValue, error) {
	switch caseName {
	case "str":
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_Str{Str: val}}, nil
	case "int":
		var v int64
		if _, err := fmt.Sscanf(val, "%d", &v); err != nil {
			return nil, err
		}
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_Int{Int: v}}, nil
	case "double":
		var v float64
		if _, err := fmt.Sscanf(val, "%f", &v); err != nil {
			return nil, err
		}
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_Double{Double: v}}, nil
	case "bool":
		lower := strings.ToLower(val)
		b := lower == "true" || lower == "1" || lower == "yes"
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_Bool{Bool: b}}, nil
	case "jsonObj":
		// ensure valid JSON
		if !json.Valid([]byte(val)) {
			return nil, fmt.Errorf("invalid JSON object")
		}
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_JsonObj{JsonObj: val}}, nil
	case "jsonArr":
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_JsonArr{JsonArr: val}}, nil
	case "bytes":
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_Bytes{Bytes: []byte(val)}}, nil
	case "wfRunId":
		wr := &lhproto.WfRunId{}
		if strings.HasPrefix(val, "{") {
			if err := protojson.Unmarshal([]byte(val), wr); err != nil {
				return nil, err
			}
		} else {
			wr.Id = val
		}
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_WfRunId{WfRunId: wr}}, nil
	case "utcTimestamp":
		// accept RFC3339
		t, err := time.Parse(time.RFC3339, val)
		if err != nil {
			return nil, err
		}
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_UtcTimestamp{UtcTimestamp: timestamppb.New(t)}}, nil
	case "struct":
		// expect JSON for Struct proto
		s := &lhproto.Struct{}
		if err := protojson.Unmarshal([]byte(val), s); err != nil {
			return nil, err
		}
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_Struct{Struct: s}}, nil
	default:
		// fallback to string
		return &lhproto.VariableValue{Value: &lhproto.VariableValue_Str{Str: val}}, nil
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
