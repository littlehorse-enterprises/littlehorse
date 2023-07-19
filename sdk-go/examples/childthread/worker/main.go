package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/childthread"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/taskworker"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var parentWorker, childWorker *taskworker.LHTaskWorker
	var err error

	parentWorker, err = taskworker.NewTaskWorker(config, childthread.ParentThreadTask, "parent-thread-task")
	if err != nil {
		log.Fatal(err)
	}

	childWorker, err = taskworker.NewTaskWorker(config, childthread.ChildThreadTask, "child-thread-task")
	if err != nil {
		log.Fatal(err)
	}

	parentWorker.RegisterTaskDef(true)
	childWorker.RegisterTaskDef(true)

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
