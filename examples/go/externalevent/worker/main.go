package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/externalevent"
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
