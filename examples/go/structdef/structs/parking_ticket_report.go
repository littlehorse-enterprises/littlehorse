package structs

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const ParkingTicketReportStructDefName string = "parking-ticket-report"
const ParkingTicketReportStructDefDescription string = "ParkingTicketReport represents a parking ticket report for a vehicle."

// ParkingTicketReport represents a parking ticket report for a vehicle.
type ParkingTicketReport struct {
	VehicleMake        string `json:"vehicleMake"`
	VehicleModel       string `json:"vehicleModel"`
	LicensePlateNumber string `json:"licensePlateNumber"`
}

func (ParkingTicketReport) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: ParkingTicketReportStructDefName, Description: ParkingTicketReportStructDefDescription}
}

func (r ParkingTicketReport) String() string {
	return fmt.Sprintf("%s %s, Plate Number: %s", r.VehicleMake, r.VehicleModel, r.LicensePlateNumber)
}
