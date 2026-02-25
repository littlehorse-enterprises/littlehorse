package main

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
	"github.com/littlehorse-enterprises/littlehorse/examples/go/structdef"
)

func main() {
	_, client := examples.LoadConfigAndClient()

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

	// RegisterWfSpec handles ExternalEventDefs, WorkflowEventDefs, and the WfSpec itself.
	wf := littlehorse.NewWorkflow(structdef.MyWorkflow, structdef.WorkflowName)
	wf.RegisterWfSpec(*client)
}
