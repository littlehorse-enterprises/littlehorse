package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/interrupt"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/taskworker"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var reportWorker, childWorker *taskworker.LHTaskWorker
	var err error

	reportWorker, err = taskworker.NewTaskWorker(config, interrupt.ReportTheResult, "report-the-result")
	if err != nil {
		log.Fatal(err)
	}

	childWorker, err = taskworker.NewTaskWorker(config, interrupt.ChildFooTask, "child-foo-task")
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
