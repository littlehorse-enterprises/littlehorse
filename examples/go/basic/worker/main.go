package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/basic"
)

func main() {
	config, _ := examples.LoadConfigAndClient()

	tw, err := littlehorse.NewTaskWorker(config, basic.Greet, basic.TaskDefName)
	if err != nil {
		log.Fatal(err)
	}

	// Create the TaskDef
	tw.RegisterTaskDef()

	log.Default().Print("Starting Task Worker")
	tw.Start()

}
