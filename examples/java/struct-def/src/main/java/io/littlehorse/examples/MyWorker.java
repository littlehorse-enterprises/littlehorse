package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("get-car-owner")
    public Person getCarOwner(ParkingTicketReport report) {
        return lookupCarOwnerInDb(report.getLicensePlateNumber());
    }

    @LHTaskMethod("mail-ticket")
    public String mailTicket(Person person) {
        log.debug("Sending mail to %s at address %s".formatted(person, person.getHomeAddress()));
        return "Ticket sent to %s".formatted(person);
    }

    // Simulates a database lookup...
    private Person lookupCarOwnerInDb(String licensePlateNumber) {
        return new Person("Obi-Wan", "Kenobi", new Address(124, "Sand Dune Lane", "Anchorhead", "Tattooine", 97412));
    }
}
