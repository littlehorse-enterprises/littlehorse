package main

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/examples/basic"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	tw, err := littlehorse.NewTaskWorker(config, basic.Greet, "greet")
	if err != nil {
		log.Fatal(err)
	}

	// Create the TaskDef
	tw.RegisterTaskDef()

	log.Default().Print("Starting Task Worker")
	tw.Start()

}
