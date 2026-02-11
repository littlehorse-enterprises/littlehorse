using LittleHorse.Sdk.Worker;

namespace StructDefExample;

[LHStructDef("person")]
public class Person
{
    public string FirstName { get; set; } = string.Empty;
    public string LastName { get; set; } = string.Empty;
    public Address HomeAddress { get; set; } = new();

    public Person()
    {
    }

    public Person(string firstName, string lastName, Address homeAddress)
    {
        FirstName = firstName;
        LastName = lastName;
        HomeAddress = homeAddress;
    }

    public override string ToString()
    {
        return $"{FirstName} {LastName}";
    }
}
