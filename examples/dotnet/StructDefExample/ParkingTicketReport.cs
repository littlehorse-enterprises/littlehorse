using LittleHorse.Sdk.Worker;

namespace StructDefExample;

[LHStructDef("parking-ticket-report", "A parking ticket report capturing vehicle details and license plate at the time of the violation.")]
public class ParkingTicketReport
{
    [LHStructField(description: "The vehicle's manufacturer or brand name, ex: Suzuki")]
    public string VehicleMake { get; set; } = string.Empty;

    [LHStructField(description: "The specific model and iteration of the vehicle, ex: Swift")]
    public string VehicleModel { get; set; } = string.Empty;

    [LHStructField(description: "The license plate number for the vehicle at the time of writing the report, ex: C90-ELE")]
    public string LicensePlateNumber { get; set; } = string.Empty;

    public ParkingTicketReport()
    {
    }

    public ParkingTicketReport(string vehicleMake, string vehicleModel, string licensePlateNumber)
    {
        VehicleMake = vehicleMake;
        VehicleModel = vehicleModel;
        LicensePlateNumber = licensePlateNumber;
    }

    public override string ToString()
    {
        return $"{VehicleMake} {VehicleModel}, Plate Number: {LicensePlateNumber}";
    }
}
