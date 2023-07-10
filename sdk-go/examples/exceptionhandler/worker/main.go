package main

import (
	"log"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/examples"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/examples/exceptionhandler"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/taskworker"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var flakyWorker, childWorker *taskworker.LHTaskWorker
	var err error

	flakyWorker, err = taskworker.NewTaskWorker(config, exceptionhandler.FlakyTask, "flaky-task")
	if err != nil {
		log.Fatal(err)
	}

	childWorker, err = taskworker.NewTaskWorker(config, exceptionhandler.SomeStableTask, "some-stable-task")
	if err != nil {
		log.Fatal(err)
	}

	flakyWorker.RegisterTaskDef(true)
	childWorker.RegisterTaskDef(true)

	defer func() {
		log.Default().Print("Shutting down task workers")
		flakyWorker.Close()
		childWorker.Close()
	}()

	// Start the workers.
	log.Default().Print("Starting Task Workers")
	go func() {
		childWorker.Start()
	}()
	flakyWorker.Start()
}
