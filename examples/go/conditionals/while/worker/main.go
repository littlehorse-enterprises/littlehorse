package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/conditionals/while"
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
