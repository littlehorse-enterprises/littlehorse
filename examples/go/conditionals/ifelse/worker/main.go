package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/conditionals/ifelse"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var saladWorker, donutWorker *littlehorse.LHTaskWorker
	var err error

	saladWorker, err = littlehorse.NewTaskWorker(config, ifelse.Salad, ifelse.EatSaladTaskName)
	if err != nil {
		log.Fatal(err)
	}

	donutWorker, err = littlehorse.NewTaskWorker(config, ifelse.Donut, ifelse.EatAnotherDonutTaskName)
	if err != nil {
		log.Fatal(err)
	}

	saladWorker.RegisterTaskDef()
	donutWorker.RegisterTaskDef()

	defer func() {
		log.Default().Print("Shutting down task workers")
		saladWorker.Close()
		donutWorker.Close()
	}()

	// Start the workers.
	log.Default().Print("Starting Task Workers")
	go func() {
		donutWorker.Start()
	}()
	saladWorker.Start()
}
