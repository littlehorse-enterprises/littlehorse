package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/externalevent"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	var err error

	greetWorker, err := littlehorse.NewTaskWorker(config, externalevent.SpecificGreeting, externalevent.SpecificGreetingTaskName)
	if err != nil {
		log.Fatal(err)
	}
	summaryWorker, err := littlehorse.NewTaskWorker(config, externalevent.ShowSummary, externalevent.SummaryTaskName)
	if err != nil {
		log.Fatal(err)
	}

	greetWorker.RegisterTaskDef()
	summaryWorker.RegisterTaskDef()

	defer func() {
		log.Default().Print("Shutting down task workers")
		greetWorker.Close()
		summaryWorker.Close()
	}()

	log.Default().Print("Starting Task Workers")
	go func() {
		greetWorker.Start()
	}()
	summaryWorker.Start()
}
