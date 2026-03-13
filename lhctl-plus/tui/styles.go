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
