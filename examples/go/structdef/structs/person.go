package structs

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const PersonStructDefName string = "person"
const PersonStructDefDescription string = "Person represents a person with a name and home address."

// Person represents a person with a name and home address.
type Person struct {
	FirstName   string  `json:"firstName"`
	LastName    string  `json:"lastName"`
	HomeAddress Address `json:"homeAddress" lh:",masked"`
}

func (Person) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: PersonStructDefName, Description: PersonStructDefDescription}
}

func (p Person) String() string {
	return fmt.Sprintf("%s %s", p.FirstName, p.LastName)
}
