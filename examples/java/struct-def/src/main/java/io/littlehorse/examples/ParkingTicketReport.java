package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;

@LHStructDef(
        value = "parking-ticket-report",
        description =
                "A parking ticket report capturing vehicle details and license plate at the time of the violation.")
public record ParkingTicketReport(
        @LHStructField(description = "The vehicle's manufacturer or brand name, ex: Suzuki") String vehicleMake,
        @LHStructField(description = "The specific model and iteration of the vehicle, ex: Swift") String vehicleModel,
        @LHStructField(
                        description =
                                "The license plate number for the vehicle at the time of writing the report, ex: C90-ELE")
                String licensePlateNumber) {
    @Override
    public String toString() {
        return String.format("%s %s, Plate Number: %s", vehicleMake, vehicleModel, licensePlateNumber);
    }
}
