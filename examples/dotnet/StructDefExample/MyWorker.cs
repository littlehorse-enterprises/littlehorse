using LittleHorse.Sdk.Worker;

namespace StructDefExample;

public class MyWorker
{
    [LHTaskMethod("get-car-owner")]
    public async Task<Person> GetCarOwner(ParkingTicketReport report)
    {
        return LookupCarOwnerInDb(report.LicensePlateNumber);
    }

    [LHTaskMethod("mail-ticket")]
    public async Task<string> MailTicket(Person person)
    {
        Console.WriteLine($"Notifying {person} of parking ticket.");
        return $"Ticket sent to {person} at {person.HomeAddress}";
    }

    private static Person LookupCarOwnerInDb(string licensePlateNumber)
    {
        return new Person("Obi-Wan", "Kenobi", new Address(124, "Sand Dune Lane", "Anchorhead", "Tattooine", 97412));
    }
}
