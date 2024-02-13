package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/jsonarray"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/taskworker"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	tw, err := taskworker.NewTaskWorker(config, jsonarray.AddUpList, "greet")

	if err != nil {
		log.Fatal(err)
	}

	// Create the TaskDef
	tw.RegisterTaskDef()

	defer func() {
		log.Default().Print("Shutting down task worker")
		tw.Close()
	}()

	log.Default().Print("Starting Task Worker")
	tw.Start()

}
