package main

import (
	"log"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/examples"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/examples/externalevent"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/taskworker"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var askForNameWorker, greetWorker *taskworker.LHTaskWorker
	var err error

	askForNameWorker, err = taskworker.NewTaskWorker(config, externalevent.AskForName, "ask-for-name")
	if err != nil {
		log.Fatal(err)
	}

	greetWorker, err = taskworker.NewTaskWorker(config, externalevent.SpecificGreeting, "specific-greeting")
	if err != nil {
		log.Fatal(err)
	}

	askForNameWorker.RegisterTaskDef(true)
	greetWorker.RegisterTaskDef(true)

	defer func() {
		log.Default().Print("Shutting down task workers")
		askForNameWorker.Close()
		greetWorker.Close()
	}()

	// Start the workers.
	log.Default().Print("Starting Task Workers")
	go func() {
		greetWorker.Start()
	}()
	askForNameWorker.Start()
}
