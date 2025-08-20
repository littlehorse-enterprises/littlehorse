package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/childthread"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var parentWorker, childWorker *littlehorse.LHTaskWorker
	var err error

	parentWorker, err = littlehorse.NewTaskWorker(config, childthread.ParentThreadTask, childthread.ParentTaskName)
	if err != nil {
		log.Fatal(err)
	}

	childWorker, err = littlehorse.NewTaskWorker(config, childthread.ChildThreadTask, childthread.ChildTaskName)
	if err != nil {
		log.Fatal(err)
	}

	parentWorker.RegisterTaskDef()
	childWorker.RegisterTaskDef()

	defer func() {
		log.Default().Print("Shutting down task workers")
		parentWorker.Close()
		childWorker.Close()
	}()

	// Start the workers.
	log.Default().Print("Starting Task Workers")
	go func() {
		childWorker.Start()
	}()
	parentWorker.Start()
}
