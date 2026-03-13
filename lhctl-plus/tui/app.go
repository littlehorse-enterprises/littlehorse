package tui

import (
	"fmt"
	"strings"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
	"github.com/littlehorse-enterprises/lhctl-plus/reflection"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type view int

const (
	viewBrowser view = iota
	viewForm
	viewResponse
)

const statusBarHeight = 1

// App is the root Bubble Tea model.
type App struct {
	conn        *grpc.ClientConn
	refClient   *reflection.Client
	rpcs        []reflection.RPCInfo
	currentView view
	returnView  view
	browser     browserModel
	form        formModel
	response    responseModel
	width       int
	height      int
	err         error
	errorView   startupErrorView
}

type startupErrorView struct {
	title  string
	detail string
	hints  []string
	raw    string
}

// messages
type rpcsLoadedMsg struct {
	rpcs []reflection.RPCInfo
}

type rpcErrorMsg struct {
	err error
}

type startupErrorMsg struct {
	err error
}

type rpcResponseMsg struct {
	json    string
	isError bool
}

func NewApp(conn *grpc.ClientConn) App {
	return App{
		conn:        conn,
		refClient:   reflection.NewClient(conn),
		currentView: viewBrowser,
		returnView:  viewBrowser,
		browser:     newBrowserModel(conn),
	}
}

func (a App) Init() tea.Cmd {
	return a.loadRPCs
}

func (a App) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.KeyMsg:
		if a.err != nil {
			switch msg.String() {
			case "q", "ctrl+c":
				return a, tea.Quit
			case "r":
				a.err = nil
				a.errorView = startupErrorView{}
				return a, a.loadRPCs
			}
		}

		switch msg.String() {
		case "ctrl+c":
			return a, tea.Quit
		case "esc":
			if a.currentView == viewForm {
				a.currentView = viewBrowser
				return a, nil
			}
			if a.currentView == viewResponse {
				a.currentView = a.returnView
				return a, nil
			}
		}

	case tea.WindowSizeMsg:
		a.width = msg.Width
		a.height = msg.Height
		contentHeight := maxInt(msg.Height-statusBarHeight, 0)
		contentWidth, contentInnerHeight := contentInnerSize(msg.Width, contentHeight)
		a.browser.setSize(contentWidth, contentInnerHeight)
		a.form.setSize(contentWidth, contentInnerHeight)
		a.response.setSize(contentWidth, contentInnerHeight)
		return a, nil

	case tea.MouseMsg:
		// Intentionally consume mouse events (including wheel) to keep interaction
		// inside the TUI in alt-screen mode.
		return a, nil

	case rpcsLoadedMsg:
		a.rpcs = msg.rpcs
		a.browser.setItems(msg.rpcs)
		return a, nil

	case startupErrorMsg:
		a.err = msg.err
		a.errorView = makeStartupErrorView(msg.err)
		return a, nil

	case rpcErrorMsg:
		a.returnView = a.currentView
		contentHeight := maxInt(a.height-statusBarHeight, 0)
		contentWidth, contentInnerHeight := contentInnerSize(a.width, contentHeight)
		a.response = newResponseModel(msg.err.Error(), contentWidth, contentInnerHeight, true)
		if a.returnView == viewBrowser {
			a.browser.exitPreviewEditMode()
		}
		a.currentView = viewResponse
		return a, nil

	case rpcResponseMsg:
		a.returnView = a.currentView
		contentHeight := maxInt(a.height-statusBarHeight, 0)
		contentWidth, contentInnerHeight := contentInnerSize(a.width, contentHeight)
		a.response = newResponseModel(msg.json, contentWidth, contentInnerHeight, msg.isError)
		if a.returnView == viewBrowser {
			a.browser.exitPreviewEditMode()
		}
		a.currentView = viewResponse
		return a, nil
	}

	switch a.currentView {
	case viewBrowser:
		var cmd tea.Cmd
		a.browser, cmd = a.browser.update(msg)
		return a, cmd

	case viewForm:
		var cmd tea.Cmd
		a.form, cmd = a.form.update(msg)
		return a, cmd

	case viewResponse:
		var cmd tea.Cmd
		a.response, cmd = a.response.update(msg)
		return a, cmd
	}

	return a, nil
}

func (a App) View() string {
	if a.err != nil {
		return a.renderErrorView()
	}

	var content string
	switch a.currentView {
	case viewBrowser:
		content = a.browser.view()
	case viewForm:
		content = a.form.view()
	case viewResponse:
		content = a.response.view()
	}

	statusBar := a.renderStatusBar()
	contentAreaHeight := maxInt(a.height-lipgloss.Height(statusBar), 0)
	innerWidth, innerHeight := contentInnerSize(a.width, contentAreaHeight)
	placedContent := lipgloss.Place(innerWidth, innerHeight, lipgloss.Left, lipgloss.Top, content)
	framedContent := contentFrameStyle.Render(placedContent)
	contentPane := lipgloss.Place(a.width, contentAreaHeight, lipgloss.Left, lipgloss.Top, framedContent)
	return lipgloss.JoinVertical(lipgloss.Left, contentPane, statusBar)
}

func contentInnerSize(totalWidth, totalHeight int) (int, int) {
	hInset := contentFrameStyle.GetHorizontalFrameSize()
	vInset := contentFrameStyle.GetVerticalFrameSize()
	innerWidth := maxInt(totalWidth-hInset, 0)
	innerHeight := maxInt(totalHeight-vInset, 0)
	return innerWidth, innerHeight
}

func (a App) renderErrorView() string {
	if a.width == 0 {
		a.width = 100
	}

	var hints strings.Builder
	for _, h := range a.errorView.hints {
		hints.WriteString("• ")
		hints.WriteString(h)
		hints.WriteString("\n")
	}

	body := lipgloss.JoinVertical(
		lipgloss.Left,
		titleStyle.Render("lhctl+"),
		errorTitleStyle.Render(a.errorView.title),
		descriptionStyle.Render(a.errorView.detail),
		"",
		helpStyle.Render(hints.String()),
		"",
		rawErrorStyle.Render(a.errorView.raw),
		"",
		helpStyle.Render("Press r to retry, q to quit."),
	)

	card := errorCardStyle.Width(minInt(a.width-4, 100)).Render(body)
	return lipgloss.Place(a.width, maxInt(a.height, 20), lipgloss.Center, lipgloss.Center, card)
}

func (a App) renderStatusBar() string {
	var left, center, right string
	switch a.currentView {
	case viewBrowser:
		left = " lhctl+  │  RPC Browser"
		center = ""
		right = "↑/↓ navigate  │  / filter  │  enter select  │  q quit "
	case viewForm:
		left = " lhctl+  │  RPC Form"
		center = ""
		right = "tab next  │  shift+tab prev  │  enter submit  │  esc back "
	case viewResponse:
		left = " lhctl+  │  Response"
		// center is for context-specific actions (e.g., copy)
		center = "c copy"
		right = "↑/↓ scroll  │  esc back  │  q quit "
	}

	// Account for the status bar style's full frame (borders + padding).
	// Use textContentWidth to get the true text area inside the bar.
	availWidth := textContentWidth(statusBarStyle, maxInt(0, a.width-statusBarStyle.GetHorizontalFrameSize()))

	leftW := lipgloss.Width(left)
	centerW := lipgloss.Width(center)
	rightW := lipgloss.Width(right)
	// account for separator between center and right when center exists
	sepW := 0
	if center != "" {
		sepW = 2 // we render two spaces between center and right
		centerW += sepW
	}

	// Prioritize keeping right, then center, then truncate left as needed.
	if rightW >= availWidth {
		right = truncateToWidth(right, availWidth)
		left = ""
		center = ""
	} else {
		remaining := availWidth - rightW

		if centerW >= remaining {
			// not enough space for center; truncate it to remaining
			center = truncateToWidth(center, maxInt(0, remaining))
			left = ""
		} else {
			// keep center, reserve remaining space for left
			remaining = remaining - centerW
			if leftW > remaining {
				left = truncateToWidth(left, remaining)
			}
		}
	}

	gap := availWidth - leftW - centerW - rightW
	if gap < 0 {
		gap = 0
	}

	// Place left, padding, then center (if present), then right.
	bar := left + fmt.Sprintf("%*s", gap, "")
	if center != "" {
		bar = bar + center + "  " + right
	} else {
		bar = bar + right
	}

	return statusBarStyle.Width(a.width).Render(bar)
}

func (a App) loadRPCs() tea.Msg {
	rpcs, err := a.refClient.ListRPCs("littlehorse.LittleHorse")
	if err != nil {
		return startupErrorMsg{err: err}
	}
	return rpcsLoadedMsg{rpcs: rpcs}
}

func makeStartupErrorView(err error) startupErrorView {
	view := startupErrorView{
		title:  "Could Not Connect To LittleHorse",
		detail: "lhctl+ could not load RPC metadata from server reflection.",
		hints: []string{
			"Start the server with ./local-dev/do-server.sh",
			"Verify host/port in ~/.config/littlehorse.config or LHC_API_HOST/LHC_API_PORT",
			"Ensure reflection is enabled on the server listener",
		},
		raw: err.Error(),
	}

	if st, ok := status.FromError(err); ok {
		switch st.Code() {
		case codes.Unavailable:
			view.detail = "The server appears to be offline or unreachable."
		case codes.Unimplemented:
			view.title = "Server Reflection Is Not Enabled"
			view.detail = "Connected to server, but gRPC reflection is unavailable."
			view.hints = []string{
				"Enable reflection in the server (ProtoReflectionServiceV1)",
				"Rebuild/restart the server",
				"Press r to retry after restart",
			}
		case codes.PermissionDenied, codes.Unauthenticated:
			view.title = "Authentication Failed"
			view.detail = "Connection reached the server, but auth was rejected."
			view.hints = []string{
				"Check OAuth/mTLS credentials in your LH config",
				"Run lhctl whoami to verify auth",
				"Press r to retry after fixing credentials",
			}
		}
	}

	lower := strings.ToLower(err.Error())
	if strings.Contains(lower, "connection refused") || strings.Contains(lower, "no such host") {
		view.detail = "The server address is not reachable from this machine."
	}

	return view
}

func minInt(a, b int) int {
	if a < b {
		return a
	}
	return b
}

func maxInt(a, b int) int {
	if a > b {
		return a
	}
	return b
}
