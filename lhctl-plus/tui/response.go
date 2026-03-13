package tui

import (
	"strings"

	"github.com/charmbracelet/bubbles/viewport"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
)

// responseModel displays the formatted RPC response.
type responseModel struct {
	viewport viewport.Model
	content  string
	width    int
	height   int
	isError  bool
}

var responsePaneStyle = lipgloss.NewStyle().
	AlignHorizontal(lipgloss.Left).
	AlignVertical(lipgloss.Top).
	BorderStyle(lipgloss.RoundedBorder()).
	BorderForeground(ColorPrimary).
	Padding(0, 1)

func newResponseModel(jsonContent string, width, height int, isError bool) responseModel {
	innerW := maxInt(width-responsePaneStyle.GetHorizontalFrameSize(), 1)
	innerH := maxInt(height-responsePaneStyle.GetVerticalFrameSize()-2, 1)
	vp := viewport.New(innerW, innerH)
	formatted := formatResponseBody(jsonContent, isError)
	vp.SetContent(formatted)
	return responseModel{
		viewport: vp,
		content:  jsonContent,
		width:    width,
		height:   height,
		isError:  isError,
	}
}

func (r *responseModel) setSize(w, h int) {
	r.width = w
	r.height = h
	r.viewport.Width = maxInt(w-responsePaneStyle.GetHorizontalFrameSize(), 1)
	r.viewport.Height = maxInt(h-responsePaneStyle.GetVerticalFrameSize()-2, 1)
}

func (r responseModel) update(msg tea.Msg) (responseModel, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.KeyMsg:
		switch msg.String() {
		case "q":
			return r, tea.Quit
		}
	}

	var cmd tea.Cmd
	r.viewport, cmd = r.viewport.Update(msg)
	return r, cmd
}

func (r responseModel) view() string {
	header := titleStyle.Render("⚡ Response")
	innerW := maxInt(r.width-responsePaneStyle.GetHorizontalFrameSize(), 1)
	innerH := maxInt(r.height-responsePaneStyle.GetVerticalFrameSize(), 1)
	body := header + "\n" + r.viewport.View()
	body = clipToLines(body, innerH)

	paneStyle := responsePaneStyle
	if r.isError {
		paneStyle = paneStyle.BorderForeground(ColorError)
	} else {
		paneStyle = paneStyle.BorderForeground(ColorSuccess)
	}

	return paneStyle.
		Width(innerW).
		Height(innerH).
		Render(body)
}

func formatResponseBody(s string, isError bool) string {
	if isError {
		return errorStyle.Render("✗ RPC failed") + "\n\n" + s
	}
	return successStyle.Render("✓ RPC succeeded") + "\n\n" + formatJSON(s)
}

// formatJSON does basic pretty-printing of JSON content.
func formatJSON(s string) string {
	var b strings.Builder
	indent := 0
	inString := false
	escaped := false

	for i := 0; i < len(s); i++ {
		c := s[i]

		if escaped {
			b.WriteByte(c)
			escaped = false
			continue
		}

		if c == '\\' && inString {
			b.WriteByte(c)
			escaped = true
			continue
		}

		if c == '"' {
			inString = !inString
			b.WriteByte(c)
			continue
		}

		if inString {
			b.WriteByte(c)
			continue
		}

		switch c {
		case '{', '[':
			b.WriteByte(c)
			indent++
			b.WriteByte('\n')
			writeIndent(&b, indent)
		case '}', ']':
			indent--
			b.WriteByte('\n')
			writeIndent(&b, indent)
			b.WriteByte(c)
		case ',':
			b.WriteByte(c)
			b.WriteByte('\n')
			writeIndent(&b, indent)
		case ':':
			b.WriteString(": ")
		default:
			b.WriteByte(c)
		}
	}

	return b.String()
}

func writeIndent(b *strings.Builder, level int) {
	for range level {
		b.WriteString("  ")
	}
}
