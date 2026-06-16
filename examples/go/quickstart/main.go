package main

import (
	"context"
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	examples "github.com/littlehorse-enterprises/littlehorse/examples/go"
)

func main() {
	config, client := examples.LoadConfigAndClient()
	workers := mustCreateWorkers(config)

	(*client).PutExternalEventDef(context.Background(),
		&lhproto.PutExternalEventDefRequest{
			Name: IDENTITY_VERIFIED_EVENT,
			CorrelatedEventConfig: &lhproto.CorrelatedEventConfig{
				DeleteAfterFirstCorrelation: true,
			},
		},
	)

	for _, worker := range workers {
		if err := worker.RegisterTaskDef(); err != nil {
			log.Fatal(err)
		}
	}

	workflow := littlehorse.NewWorkflow(QuickstartWorkflow, WORKFLOW_NAME)
	putWf, err := workflow.Compile()
	if err != nil {
		log.Fatal(err)
	}

	if _, err = (*client).PutWfSpec(context.Background(), putWf); err != nil {
		log.Fatal(err)
	}

	log.Print("Registered quickstart metadata and starting task workers.")
	for _, worker := range workers {
		go func(worker *littlehorse.LHTaskWorker) {
			if err := worker.Start(); err != nil {
				log.Print(err)
			}
		}(worker)
	}

	select {}
}

func mustCreateWorkers(config *littlehorse.LHConfig) []*littlehorse.LHTaskWorker {
	verifyIdentityWorker, err := littlehorse.NewTaskWorker(config, VerifyIdentity, VERIFY_IDENTITY_TASK)
	if err != nil {
		log.Fatal(err)
	}

	notifyCustomerVerifiedWorker, err := littlehorse.NewTaskWorker(config, NotifyCustomerVerified, NOTIFY_CUSTOMER_VERIFIED_TASK)
	if err != nil {
		log.Fatal(err)
	}

	notifyCustomerNotVerifiedWorker, err := littlehorse.NewTaskWorker(config, NotifyCustomerNotVerified, NOTIFY_CUSTOMER_NOT_VERIFIED_TASK)
	if err != nil {
		log.Fatal(err)
	}

	return []*littlehorse.LHTaskWorker{
		verifyIdentityWorker,
		notifyCustomerVerifiedWorker,
		notifyCustomerNotVerifiedWorker,
	}
}
