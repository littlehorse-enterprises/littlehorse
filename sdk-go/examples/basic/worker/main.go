package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/basic"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/taskworker"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	tw, err := taskworker.NewTaskWorker(config, basic.Greet, "greet")
	if err != nil {
		log.Fatal(err)
	}

	// Create the TaskDef
	tw.RegisterTaskDef()

	log.Default().Print("Starting Task Worker")
	tw.Start()

}
