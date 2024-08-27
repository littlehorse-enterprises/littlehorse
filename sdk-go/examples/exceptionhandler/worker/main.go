package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/exceptionhandler"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var flakyWorker, childWorker *littlehorse.LHTaskWorker
	var err error

	flakyWorker, err = littlehorse.NewTaskWorker(config, exceptionhandler.FlakyTask, exceptionhandler.FlakyTaskName)
	if err != nil {
		log.Fatal(err)
	}

	childWorker, err = littlehorse.NewTaskWorker(config, exceptionhandler.SomeStableTask, exceptionhandler.StableTaskName)
	if err != nil {
		log.Fatal(err)
	}

	flakyWorker.RegisterTaskDef()
	childWorker.RegisterTaskDef()

	defer func() {
		log.Default().Print("Shutting down task workers")
		flakyWorker.Close()
		childWorker.Close()
	}()

	// Start the workers.
	log.Default().Print("Starting Task Workers")
	go func() {
		childWorker.Start()
	}()
	flakyWorker.Start()
}
