using LittleHorse.Sdk.Worker;

namespace StructDefExample;

[LHStructDef("address")]
public class Address
{
    public int HouseNumber { get; set; }
    public string Street { get; set; } = string.Empty;
    public string City { get; set; } = string.Empty;
    public string Planet { get; set; } = string.Empty;
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
