package structs

import (
	"fmt"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

const AddressStructDefName string = "address"
const AddressStructDefDescription string = "Address represents a physical address."

// Address represents a physical address.
type Address struct {
	HouseNumber int    `json:"houseNumber"`
	Street      string `json:"street"`
	City        string `json:"city"`
	Planet      string `json:"planet"`
	ZipCode     int    `json:"zipCode"`
}

func (Address) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: AddressStructDefName, Description: AddressStructDefDescription}
}

func (a Address) String() string {
	return fmt.Sprintf("%d %s, %s, %s %d", a.HouseNumber, a.Street, a.City, a.Planet, a.ZipCode)
}
