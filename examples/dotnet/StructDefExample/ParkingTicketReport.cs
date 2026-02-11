using LittleHorse.Sdk.Worker;

namespace StructDefExample;

[LHStructDef("car")]
public class ParkingTicketReport
{
    public string VehicleMake { get; set; } = string.Empty;
    public string VehicleModel { get; set; } = string.Empty;
    public string LicensePlateNumber { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }

    public ParkingTicketReport()
    {
    }

    public ParkingTicketReport(string vehicleMake, string vehicleModel, string licensePlateNumber, DateTime createdAt)
    {
        VehicleMake = vehicleMake;
        VehicleModel = vehicleModel;
        LicensePlateNumber = licensePlateNumber;
        CreatedAt = createdAt;
    }

    public override string ToString()
    {
        return $"{VehicleMake} {VehicleModel}, Plate Number: {LicensePlateNumber}, issued at {CreatedAt:O}";
    }
}
