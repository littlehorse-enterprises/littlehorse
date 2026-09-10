using LittleHorse.Sdk.Worker;

namespace StructDefExample;

[LHStructDef("person", "A person with a name and home address.")]
public class Person
{
    [LHStructField(description: "This is the first name of the person, i.e. their given name.")]
    public string FirstName { get; set; } = string.Empty;

    [LHStructField(description: "This is the last name of the person, i.e. their family name.")]
    public string LastName { get; set; } = string.Empty;

    [LHStructField(masked: true, description: "The home address of the person.")]
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
