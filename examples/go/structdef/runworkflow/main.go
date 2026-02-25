package main

import (
	"context"
	"log"
	"os"

	"github.com/littlehorse-enterprises/littlehorse/examples/go/structdef"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func main() {
	config, err := littlehorse.NewConfigFromProps("${HOME}/.config/littlehorse.config")
	if err != nil {
		log.Fatal(err)
	}
	client, err := config.GetGrpcClient()
	if err != nil {
		log.Fatal(err)
	}

	vehicleMake := "Toyota"
	vehicleModel := "Camry"
	licensePlateNumber := "ABC-123"

	if len(os.Args) >= 4 {
		vehicleMake = os.Args[1]
		vehicleModel = os.Args[2]
		licensePlateNumber = os.Args[3]
	}

	report := structdef.ParkingTicketReport{
		VehicleMake:        vehicleMake,
		VehicleModel:       vehicleModel,
		LicensePlateNumber: licensePlateNumber,
	}

	// Serialize Go struct as a LittleHorse Struct proto
	reportStruct, err := littlehorse.GoStructToStructProto(report)
	if err != nil {
		log.Fatal(err)
	}

	wfId, err := (*client).RunWf(
		context.Background(),
		&lhproto.RunWfRequest{
			WfSpecName: structdef.WorkflowName,
			Variables: map[string]*lhproto.VariableValue{
				"ticket-report": {
					Value: &lhproto.VariableValue_Struct{
						Struct: reportStruct,
					},
				},
			},
		})
	if err != nil {
		log.Fatal(err)
	}

	log.Println("got wfRun Id:", wfId)
}
