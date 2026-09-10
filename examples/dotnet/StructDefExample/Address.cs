using LittleHorse.Sdk.Worker;

namespace StructDefExample;

[LHStructDef("address", "A physical address, including street, city, planet, and postal code.")]
public class Address
{
    [LHStructField(description: "The street number of the building.")]
    public int HouseNumber { get; set; }

    [LHStructField(description: "The name of the street.")]
    public string Street { get; set; } = string.Empty;

    [LHStructField(description: "The city or municipality.")]
    public string City { get; set; } = string.Empty;

    [LHStructField(description: "The planet where the address is located.")]
    public string Planet { get; set; } = string.Empty;

    [LHStructField(description: "The postal code for the location.")]
    public int ZipCode { get; set; }

    public Address()
    {
    }

    public Address(int houseNumber, string street, string city, string planet, int zipCode)
    {
        HouseNumber = houseNumber;
        Street = street;
        City = city;
        Planet = planet;
        ZipCode = zipCode;
    }

    public override string ToString()
    {
        return $"{HouseNumber} {Street}, {City}, {Planet} {ZipCode}";
    }
}
