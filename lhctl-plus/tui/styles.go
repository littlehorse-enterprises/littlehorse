package tui

import (
	"github.com/charmbracelet/lipgloss"
)

// ThemePalette defines all colors needed by the TUI.
// Keep this centralized so adding light/high-contrast themes later is easy.
type ThemePalette struct {
	Primary         lipgloss.Color
	Secondary       lipgloss.Color
	Success         lipgloss.Color
	Error           lipgloss.Color
	Text            lipgloss.Color
	Muted           lipgloss.Color
	Placeholder     lipgloss.Color
	Background      lipgloss.Color
	Surface         lipgloss.Color
	Border          lipgloss.Color
	ErrorBorder     lipgloss.Color
	ErrorSurface    lipgloss.Color
	RawErrorText    lipgloss.Color
	RawErrorBorder  lipgloss.Color
	RawErrorSurface lipgloss.Color
}

// Theme wraps a named palette.
type Theme struct {
	Name    string
	Palette ThemePalette
}

// Default dark theme tuned for readability on dark terminals.
var darkTheme = Theme{
	Name: "dark",
	Palette: ThemePalette{
		Primary:         lipgloss.Color("#A78BFA"),
		Secondary:       lipgloss.Color("#93C5FD"),
		Success:         lipgloss.Color("#34D399"),
		Error:           lipgloss.Color("#FCA5A5"),
		Text:            lipgloss.Color("#F9FAFB"),
		Muted:           lipgloss.Color("#D1D5DB"),
		Placeholder:     lipgloss.Color("#6B7280"),
		Background:      lipgloss.Color("#0B1020"),
		Surface:         lipgloss.Color("#1F2937"),
		Border:          lipgloss.Color("#4B5563"),
		ErrorBorder:     lipgloss.Color("#F87171"),
		ErrorSurface:    lipgloss.Color("#141A2A"),
		RawErrorText:    lipgloss.Color("#E5E7EB"),
		RawErrorBorder:  lipgloss.Color("#6B7280"),
		RawErrorSurface: lipgloss.Color("#111827"),
	},
}

var currentTheme = darkTheme

// Colors exposed for dynamic styling in other views.
var (
	ColorPrimary   lipgloss.Color
	ColorSecondary lipgloss.Color
	ColorSuccess   lipgloss.Color
	ColorError     lipgloss.Color
	ColorMuted     lipgloss.Color
	ColorText      lipgloss.Color
)

var (
	titleStyle        lipgloss.Style
	categoryStyle     lipgloss.Style
	descriptionStyle  lipgloss.Style
	statusBarStyle    lipgloss.Style
	errorStyle        lipgloss.Style
	successStyle      lipgloss.Style
	fieldLabelStyle   lipgloss.Style
	fieldTypeStyle    lipgloss.Style
	helpStyle         lipgloss.Style
	placeholderStyle  lipgloss.Style
	contentFrameStyle lipgloss.Style
	errorCardStyle    lipgloss.Style
	errorTitleStyle   lipgloss.Style
	rawErrorStyle     lipgloss.Style
)

func init() {
	ApplyTheme(currentTheme)
}

// ApplyTheme swaps the active theme and rebuilds derived styles.
func ApplyTheme(theme Theme) {
	currentTheme = theme
	p := currentTheme.Palette

	ColorPrimary = p.Primary
	ColorSecondary = p.Secondary
	ColorSuccess = p.Success
	ColorError = p.Error
	ColorMuted = p.Muted
	ColorText = p.Text

	// titleStyle has MarginBottom(1) which embeds a newline into the
	// rendered string. If you need a single-line header, copy this
	// style with .MarginBottom(0) before rendering.
	titleStyle = lipgloss.NewStyle().
		Bold(true).
		Foreground(p.Primary).
		MarginBottom(1)

	categoryStyle = lipgloss.NewStyle().
		Bold(true).
		Foreground(p.Secondary)

	descriptionStyle = lipgloss.NewStyle().
		Foreground(p.Muted).
		Italic(true)

	// statusBarStyle has Padding(0, 1) — use textContentWidth() when
	// laying out text inside this style, not the raw width.
	statusBarStyle = lipgloss.NewStyle().
		Foreground(p.Text).
		Background(p.Surface).
		Padding(0, 1)

	errorStyle = lipgloss.NewStyle().
		Foreground(p.Error).
		Bold(true)

	successStyle = lipgloss.NewStyle().
		Foreground(p.Success).
		Bold(true)

	fieldLabelStyle = lipgloss.NewStyle().
		Foreground(p.Primary).
		Bold(true)

	fieldTypeStyle = lipgloss.NewStyle().
		Foreground(p.Muted)

	helpStyle = lipgloss.NewStyle().
		Foreground(p.Muted)

	// placeholderStyle is slightly brighter than muted text for visibility
	placeholderStyle = lipgloss.NewStyle().
		Foreground(p.Placeholder)

	// contentFrameStyle has Padding(1, 2) — use textContentWidth() when
	// laying out text inside this style, not the raw width.
	contentFrameStyle = lipgloss.NewStyle().
		Border(lipgloss.RoundedBorder()).
		BorderForeground(p.Border).
		Padding(1, 2)

	errorCardStyle = lipgloss.NewStyle().
		Background(p.ErrorSurface).
		Border(lipgloss.RoundedBorder()).
		BorderForeground(p.ErrorBorder).
		Padding(1, 2)

	errorTitleStyle = lipgloss.NewStyle().
		Bold(true).
		Foreground(p.Error)

	rawErrorStyle = lipgloss.NewStyle().
		Foreground(p.RawErrorText).
		Background(p.RawErrorSurface).
		Border(lipgloss.NormalBorder()).
		BorderForeground(p.RawErrorBorder).
		Padding(0, 1)
}

// textContentWidth returns the number of columns available for actual text
// content inside a style. Lipgloss Width() includes padding, so passing
// style.Width(w) renders text into w columns but the usable text area is
// w minus horizontal padding. Use this whenever you need to lay out text
// (e.g. right-aligning, truncating, or spacing) inside a styled container.
//
// Example:
//
//	innerW := totalW - style.GetHorizontalFrameSize()
//	contentW := textContentWidth(style, innerW) // subtract padding
//	// Now use contentW for text layout, innerW for style.Width(innerW).Render(...)
func textContentWidth(style lipgloss.Style, widthPassedToRender int) int {
	w := widthPassedToRender - style.GetPaddingLeft() - style.GetPaddingRight()
	if w < 0 {
		return 0
	}
	return w
}
