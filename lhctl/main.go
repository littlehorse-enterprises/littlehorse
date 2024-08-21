/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package main

import "github.com/littlehorse-enterprises/littlehorse/lhctl/cmd"

var (
	version = "0.0.0-development"
	commit  = "none"
	date    = "unknown"
)

func main() {
	cmd.SetVersionInfo(version, commit, date)
	cmd.Execute()
}
