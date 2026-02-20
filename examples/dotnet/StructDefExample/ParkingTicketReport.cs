using LittleHorse.Sdk.Worker;

namespace StructDefExample;

[LHStructDef("car")]
public class ParkingTicketReport
{
    public string VehicleMake { get; set; } = string.Empty;
    public string VehicleModel { get; set; } = string.Empty;
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
