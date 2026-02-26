package structs

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const ParkingTicketReportStructDefName string = "parkingTicketReport"
const ParkingTicketReportStructDefDescription string = "ParkingTicketReport represents a parking ticket report for a vehicle."

// ParkingTicketReport represents a parking ticket report for a vehicle.
// The LicensePlateNumber field is marked as masked because it contains sensitive
// information. The "masked" option in the "lh" tag is the Go equivalent of
// Java's @LHStructField(masked = true) and .NET's [LHStructField(masked: true)].
type ParkingTicketReport struct {
	VehicleMake        string `json:"vehicleMake"`
	VehicleModel       string `json:"vehicleModel"`
	LicensePlateNumber string `lh:"licensePlateNumber,masked"`
}

func (ParkingTicketReport) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: ParkingTicketReportStructDefName, Description: ParkingTicketReportStructDefDescription}
}

func (r ParkingTicketReport) String() string {
	return fmt.Sprintf("%s %s, Plate Number: %s", r.VehicleMake, r.VehicleModel, r.LicensePlateNumber)
}
