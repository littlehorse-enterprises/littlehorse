package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.Random;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationBooker {

    private static final Logger log = LoggerFactory.getLogger(
        ReservationBooker.class
    );

    @LHTaskMethod("book-flight")
    public String bookFlight() {
        String confirmationNumber = UUID.randomUUID().toString();
        log.debug("Running book-flight. Confirmation number {}", confirmationNumber);
        return confirmationNumber;
    }

    @LHTaskMethod("cancel-flight")
    public void cancelFlight(String confirmationNumber) {
        log.debug(
            "Cancelling the fake flight with confirmation: {}",
            confirmationNumber
        );
    }

    @LHTaskMethod("book-hotel")
    public String bookHotel() {
        log.debug("Running book-hotel");
        if (new Random().nextBoolean()) {
            log.error("Error when booking hotel");
            throw new RuntimeException("Yikes, hotel failed");
        }
        String confirmationNumber = UUID.randomUUID().toString();
        log.debug("Hotel successfully booked. Confirmation {}", confirmationNumber);
        return confirmationNumber;
    }
}
