package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class NotificationWorker {

    @LHTaskMethod("notify-customer")
    public String notifyCustomer(Customer customer) {
        if (customer.getEmail() != null) {
            return "Emailed %s at %s".formatted(customer.getName(), customer.getEmail());
        }
        if (customer.getPhoneNumber() != null) {
            return "Texted %s at %s".formatted(customer.getName(), customer.getPhoneNumber());
        }
        return "No contact method for %s".formatted(customer.getName());
    }
}
