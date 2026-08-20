package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef(
        value = "parking-ticket-report",
        description =
                "A parking ticket report capturing vehicle details and license plate at the time of the violation.")
public class ParkingTicketReport {
    @LHStructField(description = "The vehicle's manufacturer or brand name, ex: Suzuki")
    private String vehicleMake;

    @LHStructField(description = "The specific model and iteration of the vehicle, ex: Swift")
    private String vehicleModel;

    @LHStructField(
            description = "The license plate number for the vehicle at the time of writing the report, ex; C90-ELE")
    private String licensePlateNumber;

    public ParkingTicketReport() {}

    public ParkingTicketReport(String vehicleMake, String vehicleModel, String licensePlateNumber, Date createdAt) {
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.licensePlateNumber = licensePlateNumber;
    }

    @Override
    public String toString() {
        return String.format("%s %s, Plate Number: %s", vehicleMake, vehicleModel, licensePlateNumber);
    }
}
