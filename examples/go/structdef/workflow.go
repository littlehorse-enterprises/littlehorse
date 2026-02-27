package structdef

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/examples/go/structdef/structs"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const WorkflowName string = "issue-parking-ticket"
const GetCarOwnerTaskName string = "get-car-owner"
const MailTicketTaskName string = "mail-ticket"

// GetCarOwner simulates looking up a car owner by their parking ticket report.
func GetCarOwner(report *structs.ParkingTicketReport) structs.Person {
	return structs.Person{
		FirstName: "Obi-Wan",
		LastName:  "Kenobi",
		HomeAddress: structs.Address{
			HouseNumber: 124,
			Street:      "Sand Dune Lane",
			City:        "Anchorhead",
			Planet:      "Tattooine",
			ZipCode:     97412,
		},
	}
}

// MailTicket simulates mailing a parking ticket to a person.
func MailTicket(person *structs.Person, city string) string {
	fmt.Printf("Notifying %s of parking ticket in %s.\n", person, city)
	return fmt.Sprintf("Ticket sent to %s at %s", person, &person.HomeAddress)
}

// MyWorkflow defines the WfSpec for issuing a parking ticket.
func MyWorkflow(wf *littlehorse.WorkflowThread) {
	ticketReport := wf.DeclareStruct("ticket-report", structs.ParkingTicketReportStructDefName).Required()
	carOwner := wf.DeclareStruct("car-owner", structs.PersonStructDefName)

	carOwner.Assign(wf.Execute(GetCarOwnerTaskName, ticketReport))

	// Use LHPath to access nested struct fields in a type-safe way.
	// This extracts carOwner.homeAddress.city via Get() chaining.
	ownerCity := carOwner.Get("homeAddress").Get("city")
	wf.Execute(MailTicketTaskName, carOwner, ownerCity)
}
