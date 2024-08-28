package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/externalevent"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var askForNameWorker, greetWorker *littlehorse.LHTaskWorker
	var err error

	askForNameWorker, err = littlehorse.NewTaskWorker(config, externalevent.AskForName, externalevent.AskForNameTaskName)
	if err != nil {
		log.Fatal(err)
	}

	greetWorker, err = littlehorse.NewTaskWorker(config, externalevent.SpecificGreeting, externalevent.SpecificGreetingTaskName)
	if err != nil {
		log.Fatal(err)
	}

	askForNameWorker.RegisterTaskDef()
	greetWorker.RegisterTaskDef()

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
