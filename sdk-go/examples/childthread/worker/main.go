package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/childthread"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var parentWorker, childWorker *littlehorse.LHTaskWorker
	var err error

	parentWorker, err = littlehorse.NewTaskWorker(config, childthread.ParentThreadTask, "parent-thread-task")
	if err != nil {
		log.Fatal(err)
	}

	childWorker, err = littlehorse.NewTaskWorker(config, childthread.ChildThreadTask, "child-thread-task")
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
