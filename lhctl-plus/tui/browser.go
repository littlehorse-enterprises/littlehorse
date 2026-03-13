package tui

import (
	"fmt"
	"strings"

	"github.com/charmbracelet/bubbles/textinput"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
	"github.com/littlehorse-enterprises/lhctl-plus/reflection"
	"google.golang.org/grpc"
)

// browserModel implements the RPC browser / command palette view.
type browserModel struct {
	rpcs     []reflection.RPCInfo
	filtered []reflection.RPCInfo
	filter   textinput.Model
	cursor   int
	width    int
	height   int
	offset   int // viewport scroll offset
	conn     *grpc.ClientConn

	previewEditing bool
	previewForm    *formModel
	previewRPCName string
}

func newBrowserModel(conn *grpc.ClientConn) browserModel {
	ti := textinput.New()
	ti.Placeholder = "Search RPCs..."
	ti.Focus()
	ti.CharLimit = 100
	return browserModel{
		filter: ti,
		conn:   conn,
	}
}

func (b *browserModel) setSize(w, h int) {
	b.width = w
	b.height = h
	b.filter.Width = w - 4
}

func (b *browserModel) setItems(rpcs []reflection.RPCInfo) {
	b.rpcs = rpcs
	b.applyFilter()
	b.syncPreviewForm()
}

func (b browserModel) selectedRPC() *reflection.RPCInfo {
	if b.cursor >= 0 && b.cursor < len(b.filtered) {
		return &b.filtered[b.cursor]
	}
	return nil
}

func (b *browserModel) applyFilter() {
	query := strings.ToLower(b.filter.Value())
	if query == "" {
		b.filtered = b.rpcs
	} else {
		b.filtered = nil
		for _, rpc := range b.rpcs {
			name := strings.ToLower(rpc.MethodName)
			cat := strings.ToLower(rpc.Category.String())
			desc := strings.ToLower(rpc.Description)
			if strings.Contains(name, query) || strings.Contains(cat, query) || strings.Contains(desc, query) {
				b.filtered = append(b.filtered, rpc)
			}
		}
	}
	if b.cursor >= len(b.filtered) {
		b.cursor = max(0, len(b.filtered)-1)
	}
	b.offset = 0
}

func (b browserModel) update(msg tea.Msg) (browserModel, tea.Cmd) {
	if b.previewEditing && b.previewForm != nil {
		switch msg := msg.(type) {
		case tea.KeyMsg:
			switch msg.String() {
			case "esc":
				b.previewEditing = false
				b.blurPreviewForm()
				b.filter.Focus()
				return b, nil
			}
		}

		updatedForm, cmd := b.previewForm.update(msg)
		b.previewForm = &updatedForm
		return b, cmd
	}

	switch msg := msg.(type) {
	case tea.KeyMsg:
		switch msg.String() {
		case "up", "k":
			if b.cursor > 0 {
				b.cursor--
				b.ensureVisible()
				b.syncPreviewForm()
			}
			return b, nil
		case "down", "j":
			if b.cursor < len(b.filtered)-1 {
				b.cursor++
				b.ensureVisible()
				b.syncPreviewForm()
			}
			return b, nil
		case "enter":
			return b.handleEnter()
		case "q":
			if !b.filter.Focused() || b.filter.Value() == "" {
				return b, tea.Quit
			}
		}
	}

	var cmd tea.Cmd
	prev := b.filter.Value()
	b.filter, cmd = b.filter.Update(msg)
	if b.filter.Value() != prev {
		b.applyFilter()
		b.syncPreviewForm()
	}
	return b, cmd
}

func (b browserModel) handleEnter() (browserModel, tea.Cmd) {
	selected := b.selectedRPC()
	if selected == nil {
		return b, nil
	}

	// Zero-input RPCs can be submitted immediately without entering edit mode.
	if selected.Input == nil || len(selected.Input.GetFields()) == 0 {
		fm := newFormModel(*selected, b.conn)
		_, rightWidth := b.splitPaneWidths()
		fm.setSize(max(1, rightWidth-4), max(1, b.height-2))
		b.previewForm = &fm
		b.previewRPCName = selected.MethodName
		b.previewEditing = false
		b.blurPreviewForm()
		b.filter.Focus()
		return b, fm.submit()
	}

	b.enterPreviewEditMode()
	return b, nil
}

func (b *browserModel) enterPreviewEditMode() {
	selected := b.selectedRPC()
	if selected == nil || b.conn == nil {
		return
	}

	fm := newFormModel(*selected, b.conn)
	_, rightWidth := b.splitPaneWidths()
	fm.setSize(max(1, rightWidth-4), max(1, b.height-2))
	if len(fm.fields) > 0 {
		fm.fields[0].input.Focus()
	}
	b.previewForm = &fm
	b.previewRPCName = selected.MethodName
	b.previewEditing = true
	b.filter.Blur()
}

func (b *browserModel) syncPreviewForm() {
	selected := b.selectedRPC()
	if selected == nil {
		b.previewForm = nil
		b.previewRPCName = ""
		b.previewEditing = false
		return
	}

	if b.previewEditing {
		// Keep user's in-progress form state while editing.
		return
	}

	if b.previewForm == nil || b.previewRPCName != selected.MethodName {
		fm := newFormModel(*selected, b.conn)
		_, rightWidth := b.splitPaneWidths()
		fm.setSize(max(1, rightWidth-4), max(1, b.height-2))
		for i := range fm.fields {
			fm.fields[i].input.Blur()
		}
		b.previewForm = &fm
		b.previewRPCName = selected.MethodName
	}
}

func (b *browserModel) blurPreviewForm() {
	if b.previewForm == nil {
		return
	}
	for i := range b.previewForm.fields {
		b.previewForm.fields[i].input.Blur()
	}
}

func (b *browserModel) exitPreviewEditMode() {
	b.previewEditing = false
	b.blurPreviewForm()
	b.filter.Focus()
}

func (b *browserModel) ensureVisible() {
	visibleRows := b.visibleRows()
	if b.cursor < b.offset {
		b.offset = b.cursor
	} else if b.cursor >= b.offset+visibleRows {
		b.offset = b.cursor - visibleRows + 1
	}
}

func (b browserModel) visibleRows() int {
	// 4 lines for title + filter + spacing; each RPC row uses one line.
	rows := b.height - 4
	if rows < 1 {
		rows = 1
	}
	return rows
}

func (b browserModel) view() string {
	b.syncPreviewForm()

	// Always render a two-pane layout: left list and right preview.
	// When there are no matching RPCs, the left pane will show a
	// "No matching RPCs found" message and the right pane will show
	// "No RPC selected." This avoids the entire preview/pane area
	// disappearing when a filter yields zero results.
	leftWidth := int(float64(max(b.width, 40)) * 0.40)
	if leftWidth < 30 {
		leftWidth = 30
	}
	rightWidth := b.width - leftWidth - 1
	if rightWidth < 30 {
		rightWidth = 30
		leftWidth = max(30, b.width-rightWidth-1)
	}

	leftPane := b.renderListPane(leftWidth)
	rightPane := b.renderPreviewPane(rightWidth)

	return lipgloss.JoinHorizontal(lipgloss.Top, leftPane, " ", rightPane)
}

func (b browserModel) splitPaneWidths() (int, int) {
	leftWidth := int(float64(max(b.width, 40)) * 0.40)
	if leftWidth < 30 {
		leftWidth = 30
	}
	rightWidth := b.width - leftWidth - 1
	if rightWidth < 30 {
		rightWidth = 30
		leftWidth = max(30, b.width-rightWidth-1)
	}
	return leftWidth, rightWidth
}

func (b browserModel) renderListOnlyView(width, height int) string {
	var s strings.Builder
	innerWidth := max(20, width-6)

	filter := b.filter
	filter.Width = innerWidth

	header := titleStyle.Render("⚡ lhctl+ — RPC Command Palette")
	s.WriteString(header)
	s.WriteString("\n")
	// When the filter is empty, render our placeholder with placeholderStyle
	// to control placeholder color instead of embedding ANSI sequences.
	if strings.TrimSpace(filter.Value()) == "" {
		s.WriteString(placeholderStyle.Render(filter.Placeholder))
	} else {
		s.WriteString(filter.View())
	}
	s.WriteString("\n\n")

	if len(b.filtered) == 0 {
		s.WriteString(descriptionStyle.Render("  No matching RPCs found."))
		return s.String()
	}

	lineBudget := max(1, height-4)
	if lineBudget < 1 {
		lineBudget = 1
	}

	// Derive a safe viewport start so the cursor is always visible,
	// even if offset got stale after resizes/view changes.
	startOffset := b.offset
	if startOffset < 0 {
		startOffset = 0
	}
	if b.cursor < startOffset {
		startOffset = b.cursor
	}
	if b.cursor >= startOffset+lineBudget {
		startOffset = b.cursor - lineBudget + 1
	}
	if startOffset < 0 {
		startOffset = 0
	}
	if startOffset >= len(b.filtered) {
		startOffset = max(0, len(b.filtered)-1)
	}

	var lastCat reflection.RPCCategory = -1
	consumedLines := 0
	end := startOffset
	for i := startOffset; i < len(b.filtered) && consumedLines < lineBudget; i++ {
		rpc := b.filtered[i]

		if rpc.Category != lastCat && consumedLines < lineBudget {
			lastCat = rpc.Category
			s.WriteString(categoryStyle.Render(fmt.Sprintf("  ── %s ──", rpc.Category.String())))
			s.WriteString("\n")
			consumedLines++
			if consumedLines >= lineBudget {
				break
			}
		}

		cursor := "  "
		row := rpc.MethodName
		if rpc.Category != reflection.CategoryOther {
			row = fmt.Sprintf("%s", rpc.MethodName)
		}
		row = truncateToWidth(row, max(8, innerWidth-4))

		nameStyle := lipgloss.NewStyle().Foreground(ColorText)
		if i == b.cursor {
			cursor = "▶ "
			nameStyle = lipgloss.NewStyle().
				Bold(true).
				Foreground(lipgloss.Color("#111827")).
				Background(ColorPrimary)
		}

		line := cursor + row
		if i == b.cursor {
			line = nameStyle.Width(max(8, innerWidth-1)).Render(line)
		} else {
			line = nameStyle.Render(line)
		}
		s.WriteString(line)
		s.WriteString("\n")
		consumedLines++
		end = i + 1
	}

	if end < len(b.filtered) {
		s.WriteString(helpStyle.Render(fmt.Sprintf("\n  %d/%d RPCs", len(b.filtered), len(b.rpcs))))
	}

	return s.String()
}

func (b browserModel) renderListPane(width int) string {
	paneStyle := lipgloss.NewStyle().
		AlignHorizontal(lipgloss.Left).
		AlignVertical(lipgloss.Top).
		BorderStyle(lipgloss.RoundedBorder()).
		BorderForeground(ColorSecondary).
		Padding(0, 1)

	innerWidth := max(1, width-paneStyle.GetHorizontalFrameSize())
	innerHeight := max(1, b.height-paneStyle.GetVerticalFrameSize())
	content := b.renderListOnlyView(width, innerHeight)
	content = clipToLines(content, innerHeight)

	return paneStyle.
		Width(innerWidth).
		Height(innerHeight).
		Render(content)
}

func (b browserModel) renderPreviewPane(width int) string {
	if b.previewForm != nil {
		b.previewForm.setSize(max(1, width-4), max(1, b.height-2))
	}

	if b.previewForm == nil {
		return b.renderPreviewContainer(helpStyle.Render("No RPC selected."), width)
	}

	content := b.previewForm.view()
	if b.previewEditing {
		content += "\n\n" + helpStyle.Render("Esc to return to list navigation.")
	} else {
		content += "\n\n" + helpStyle.Render("Enter to edit this form in-place on the right pane.")
	}

	return b.renderPreviewContainer(content, width)
}

func (b browserModel) renderPreviewContainer(content string, width int) string {

	paneStyle := lipgloss.NewStyle().
		AlignHorizontal(lipgloss.Left).
		AlignVertical(lipgloss.Top).
		BorderStyle(lipgloss.RoundedBorder()).
		BorderForeground(ColorPrimary).
		Padding(0, 1)

	innerWidth := max(1, width-paneStyle.GetHorizontalFrameSize())
	innerHeight := max(1, b.height-paneStyle.GetVerticalFrameSize())
	content = clipToLines(content, innerHeight)

	return paneStyle.
		Width(innerWidth).
		Height(innerHeight).
		Render(content)
}

func max(a, b int) int {
	if a > b {
		return a
	}
	return b
}

func truncateToWidth(s string, maxWidth int) string {
	if maxWidth <= 1 {
		return ""
	}
	r := []rune(s)
	if len(r) <= maxWidth {
		return s
	}
	if maxWidth <= 3 {
		return string(r[:maxWidth])
	}
	return string(r[:maxWidth-1]) + "…"
}

func clipToLines(content string, maxLines int) string {
	if maxLines < 1 {
		return ""
	}
	lines := strings.Split(content, "\n")
	if len(lines) <= maxLines {
		return content
	}
	return strings.Join(lines[:maxLines], "\n")
}
