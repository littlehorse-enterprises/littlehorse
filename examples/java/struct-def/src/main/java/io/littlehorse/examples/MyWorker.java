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
        if (person.getHomeAddress() == null) {
            log.debug("No address for {}. Routing ticket to manual follow-up queue.", person);
            return "Ticket queued for manual follow-up for %s".formatted(person);
        }

        log.debug("Sending mail to %s at address %s".formatted(person, person.getHomeAddress()));
        return "Ticket sent to %s".formatted(person);
    }

    // Simulates a database lookup...
    private Person lookupCarOwnerInDb(String licensePlateNumber) {
        if (licensePlateNumber.startsWith("NOADDR")) {
            // Demonstrates nullable StructDef fields: address is intentionally unknown.
            return new Person("Din", "Djarin", null);
        }

        return new Person("Obi-Wan", "Kenobi", new Address(124, "Sand Dune Lane", "Anchorhead", "Tattooine", 97412));
    }
}
