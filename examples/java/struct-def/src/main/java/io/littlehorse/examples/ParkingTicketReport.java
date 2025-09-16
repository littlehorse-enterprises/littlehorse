package io.littlehorse.examples;

import java.util.Date;

import io.littlehorse.sdk.worker.LHStructDef;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@LHStructDef(name = "car")
public class ParkingTicketReport {
    private String vehicleMake;
    private String vehicleModel;
    private String licensePlateNumber;
    private Date createdAt;

    public ParkingTicketReport() {}

    public ParkingTicketReport(String vehicleMake, String vehicleModel, String licensePlateNumber, Date createdAt) {
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.licensePlateNumber = licensePlateNumber;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("%s %s, Plate Number: %s, issued at %s", vehicleMake, vehicleModel, licensePlateNumber, createdAt.toString());
    }
}
