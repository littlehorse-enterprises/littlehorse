package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/conditionals/while"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/taskworker"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var donutWorker *taskworker.LHTaskWorker
	var err error

	donutWorker, err = taskworker.NewTaskWorker(config, while.Donut, "eat-another-donut")
	if err != nil {
		log.Fatal(err)
	}

	donutWorker.RegisterTaskDef(true)

	defer func() {
		log.Default().Print("Shutting down task workers")

		donutWorker.Close()
	}()

	// Start the workers.
	log.Default().Print("Starting Task Workers")

	donutWorker.Start()

}
