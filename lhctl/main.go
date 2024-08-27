/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package main

import "github.com/littlehorse-enterprises/lhctl/internal"

var (
	version = "0.0.0-development"
	commit  = "none"
	date    = "unknown"
)

func main() {
	internal.SetVersionInfo(version, commit, date)
	internal.Execute()
}
