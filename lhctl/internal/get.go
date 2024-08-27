/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package internal

import (
	"github.com/spf13/cobra"
)

// getCmd represents the get command
var getCmd = &cobra.Command{
	Use:   "get",
	Short: "Utility to GET LittleHorse API resources.",
}

func init() {
	rootCmd.AddCommand(getCmd)
}
