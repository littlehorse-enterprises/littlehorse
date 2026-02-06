package main

import (
	"log"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/checkpoint"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	tw, err := littlehorse.NewTaskWorker(config, checkpoint.GreetWithCheckpoints, checkpoint.TaskDefName)
	if err != nil {
		log.Fatal(err)
	}

	// Create the TaskDef
	err = tw.RegisterTaskDef()
	if err != nil {
		log.Fatal(err)
	}

	log.Default().Print("Starting Checkpoint Task Worker")
	tw.Start()
}
