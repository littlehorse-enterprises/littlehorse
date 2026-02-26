package structdef

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const WorkflowName string = "issue-parking-ticket"
const GetCarOwnerTaskName string = "get-car-owner"
const MailTicketTaskName string = "mail-ticket"

const AddressStructDefName string = "address"
const PersonStructDefName string = "person"
const ParkingTicketReportStructDefName string = "car"

// Address represents a physical address.
type Address struct {
	HouseNumber int    `json:"houseNumber"`
	Street      string `json:"street"`
	City        string `json:"city"`
	Planet      string `json:"planet"`
	ZipCode     int    `json:"zipCode"`
}

func (Address) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: AddressStructDefName}
}

func (a Address) String() string {
	return fmt.Sprintf("%d %s, %s, %s %d", a.HouseNumber, a.Street, a.City, a.Planet, a.ZipCode)
}

// Person represents a person with a name and home address.
type Person struct {
	FirstName   string  `json:"firstName"`
	LastName    string  `json:"lastName"`
	HomeAddress Address `json:"homeAddress"`
}

func (Person) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: PersonStructDefName}
}

func (p Person) String() string {
	return fmt.Sprintf("%s %s", p.FirstName, p.LastName)
}

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
	return littlehorse.LHStructDefInfo{Name: ParkingTicketReportStructDefName}
}

func (r ParkingTicketReport) String() string {
	return fmt.Sprintf("%s %s, Plate Number: %s", r.VehicleMake, r.VehicleModel, r.LicensePlateNumber)
}

// GetCarOwner simulates looking up a car owner by their parking ticket report.
func GetCarOwner(report *ParkingTicketReport) Person {
	return Person{
		FirstName: "Obi-Wan",
		LastName:  "Kenobi",
		HomeAddress: Address{
			HouseNumber: 124,
			Street:      "Sand Dune Lane",
			City:        "Anchorhead",
			Planet:      "Tattooine",
			ZipCode:     97412,
		},
	}
}

// MailTicket simulates mailing a parking ticket to a person.
func MailTicket(person *Person) string {
	fmt.Printf("Notifying %s of parking ticket.\n", person)
	return fmt.Sprintf("Ticket sent to %s at %s", person, &person.HomeAddress)
}

// MyWorkflow defines the WfSpec for issuing a parking ticket.
func MyWorkflow(wf *littlehorse.WorkflowThread) {
	ticketReport := wf.DeclareStruct("ticket-report", ParkingTicketReportStructDefName).Required()
	carOwner := wf.DeclareStruct("car-owner", PersonStructDefName)

	carOwner.Assign(wf.Execute(GetCarOwnerTaskName, ticketReport))
	wf.Execute(MailTicketTaskName, carOwner)
}
