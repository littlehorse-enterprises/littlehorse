package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/jsonobj"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	tw, err := littlehorse.NewTaskWorker(config, jsonobj.GetInfo, "greet")

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
