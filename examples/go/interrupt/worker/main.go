package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/interrupt"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var reportWorker, childWorker *littlehorse.LHTaskWorker
	var err error

	reportWorker, err = littlehorse.NewTaskWorker(config, interrupt.ReportTheResult, interrupt.ReportResultTaskName)
	if err != nil {
		log.Fatal(err)
	}

	childWorker, err = littlehorse.NewTaskWorker(config, interrupt.ChildFooTask, interrupt.ChildFooTaskName)
	if err != nil {
		log.Fatal(err)
	}

	reportWorker.RegisterTaskDef()
	childWorker.RegisterTaskDef()

	defer func() {
		log.Default().Print("Shutting down task workers")
		reportWorker.Close()
		childWorker.Close()
	}()

	// Start the workers.
	log.Default().Print("Starting Task Workers")
	go func() {
		childWorker.Start()
	}()
	reportWorker.Start()
}
