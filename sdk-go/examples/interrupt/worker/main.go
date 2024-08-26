package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/interrupt"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var reportWorker, childWorker *littlehorse.LHTaskWorker
	var err error

	reportWorker, err = littlehorse.NewTaskWorker(config, interrupt.ReportTheResult, "report-the-result")
	if err != nil {
		log.Fatal(err)
	}

	childWorker, err = littlehorse.NewTaskWorker(config, interrupt.ChildFooTask, "child-foo-task")
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
