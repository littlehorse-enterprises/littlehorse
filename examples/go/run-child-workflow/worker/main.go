package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	rcw "github.com/littlehorse-enterprises/littlehorse/examples/go/run-child-workflow"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	childWorker, err := littlehorse.NewTaskWorker(config, rcw.Greet, rcw.TaskName)
	if err != nil {
		log.Fatal(err)
	}

	parentWorker, err := littlehorse.NewTaskWorker(config, rcw.Greet, rcw.TaskName)
	if err != nil {
		log.Fatal(err)
	}

	childWorker.RegisterTaskDef()
	parentWorker.RegisterTaskDef()

	defer func() {
		log.Default().Print("Shutting down task workers")
		childWorker.Close()
		parentWorker.Close()
	}()

	log.Default().Print("Starting Task Workers")
	go func() {
		childWorker.Start()
	}()
	parentWorker.Start()
}
