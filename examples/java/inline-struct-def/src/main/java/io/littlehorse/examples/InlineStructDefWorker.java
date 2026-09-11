package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InlineStructDefWorker {

    private static final Logger log = LoggerFactory.getLogger(InlineStructDefWorker.class);

    @LHTaskMethod("normalize-order")
    public Order normalizeOrder(Order order) {
        DeliveryAddress address = order.getDeliveryAddress();
        DeliveryAddress normalizedAddress = new DeliveryAddress(
                address.getStreet().trim(),
                address.getCity().trim(),
                address.getPostalCode().trim());

        return new Order(order.getOrderId(), normalizedAddress);
    }

    @LHTaskMethod("format-shipping-label")
    public String formatShippingLabel(Order order) {
        DeliveryAddress address = order.getDeliveryAddress();
        String label = "%s: %s, %s %s"
                .formatted(order.getOrderId(), address.getStreet(), address.getCity(), address.getPostalCode());
        log.info("Prepared shipping label: {}", label);
        return label;
    }
}
