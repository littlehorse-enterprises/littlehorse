package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/structdef"
)

func main() {
	config, client := examples.LoadConfigAndClient()

	// Register StructDefs manually in dependency order (dependencies first).
	structs := []interface{}{
		structdef.Address{},
		structdef.Person{},
		structdef.ParkingTicketReport{},
	}
	for _, s := range structs {
		if err := littlehorse.RegisterStructDef(*client, s, nil); err != nil {
			log.Fatal(err)
		}
	}

	getCarOwnerWorker, err := littlehorse.NewTaskWorker(
		config, structdef.GetCarOwner, structdef.GetCarOwnerTaskName,
	)
	if err != nil {
		log.Fatal(err)
	}

	mailTicketWorker, err := littlehorse.NewTaskWorker(
		config, structdef.MailTicket, structdef.MailTicketTaskName,
	)
	if err != nil {
		log.Fatal(err)
	}

	// Register the TaskDefs
	if err := getCarOwnerWorker.RegisterTaskDef(); err != nil {
		log.Fatal(err)
	}
	if err := mailTicketWorker.RegisterTaskDef(); err != nil {
		log.Fatal(err)
	}

	log.Println("Starting Task Workers...")

	// Start workers in goroutines
	go func() {
		if err := getCarOwnerWorker.Start(); err != nil {
			log.Fatal(err)
		}
	}()

	mailTicketWorker.Start()
}
