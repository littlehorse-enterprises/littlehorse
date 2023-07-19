package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/bytes"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/taskworker"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	tw, err := taskworker.NewTaskWorker(config, bytes.Greet, "greet")

	if err != nil {
		log.Fatal(err)
	}

	// Create the TaskDef
	tw.RegisterTaskDef(true)

	defer func() {
		log.Default().Print("Shutting down task worker")
		tw.Close()
	}()

	log.Default().Print("Starting Task Worker")
	tw.Start()

}
