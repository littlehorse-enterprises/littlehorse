package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/casting"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var err error

	stringWorker, err := littlehorse.NewTaskWorker(config, StringMethod, casting.StringTask)
	if err != nil {
		log.Fatal(err)
	}
	intWorker, err := littlehorse.NewTaskWorker(config, IntMethod, casting.IntTask)
	if err != nil {
		log.Fatal(err)
	}
	doubleWorker, err := littlehorse.NewTaskWorker(config, DoubleMethod, casting.DoubleTask)
	if err != nil {
		log.Fatal(err)
	}
	boolWorker, err := littlehorse.NewTaskWorker(config, BoolMethod, casting.BoolTask)
	if err != nil {
		log.Fatal(err)
	}

	stringWorker.RegisterTaskDef()
	intWorker.RegisterTaskDef()
	doubleWorker.RegisterTaskDef()
	boolWorker.RegisterTaskDef()

	defer func() {
		log.Default().Print("Shutting down task workers")
		stringWorker.Close()
		intWorker.Close()
		doubleWorker.Close()
		boolWorker.Close()
	}()

	log.Default().Print("Starting Task Workers")
	go func() { stringWorker.Start() }()
	go func() { intWorker.Start() }()
	go func() { doubleWorker.Start() }()
	boolWorker.Start()
}
