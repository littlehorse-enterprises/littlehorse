package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/conditionals/while"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var donutWorker *littlehorse.LHTaskWorker
	var err error

	donutWorker, err = littlehorse.NewTaskWorker(config, while.Donut, while.TaskDefName)
	if err != nil {
		log.Fatal(err)
	}

	donutWorker.RegisterTaskDef()

	defer func() {
		log.Default().Print("Shutting down task workers")

		donutWorker.Close()
	}()

	// Start the workers.
	log.Default().Print("Starting Task Workers")

	donutWorker.Start()

}
