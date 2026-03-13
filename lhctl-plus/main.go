package main

import (
	"fmt"
	"os"
	"path/filepath"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/littlehorse-enterprises/lhctl-plus/tui"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func main() {
	config := loadConfig()

	url := config.ApiHost + ":" + config.ApiPort
	conn, err := config.GetGrpcConn(url)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Failed to connect to LH server: %v\n", err)
		os.Exit(1)
	}
	defer conn.Close()

	p := tea.NewProgram(
		tui.NewApp(conn),
		tea.WithAltScreen(),
		tea.WithMouseCellMotion(),
	)

	if _, err := p.Run(); err != nil {
		fmt.Fprintf(os.Stderr, "Error: %v\n", err)
		os.Exit(1)
	}
}

func loadConfig() *littlehorse.LHConfig {
	home, _ := os.UserHomeDir()
	configPath := filepath.Join(home, ".config", "littlehorse.config")

	config, err := littlehorse.NewConfigFromProps(configPath)
	if err != nil {
		config = littlehorse.NewConfigFromEnv()
	}
	return config
}
