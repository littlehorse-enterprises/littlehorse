package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef("parking-ticket-report")
public class ParkingTicketReport {
    private String vehicleMake;
    private String vehicleModel;
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
