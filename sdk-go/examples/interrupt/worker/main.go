package main

import (
	"log"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/examples"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/examples/interrupt"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/taskworker"
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

	reportWorker.RegisterTaskDef(true)
	childWorker.RegisterTaskDef(true)

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
