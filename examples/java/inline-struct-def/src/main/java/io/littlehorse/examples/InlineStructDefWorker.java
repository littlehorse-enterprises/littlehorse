package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InlineStructDefWorker {

    private static final Logger log = LoggerFactory.getLogger(InlineStructDefWorker.class);

    @LHTaskMethod("normalize-address")
    @LHType(isInlineStruct = true)
    public DeliveryAddress normalizeAddress(@LHType(isInlineStruct = true) DeliveryAddress address) {
        return new DeliveryAddress(
                address.getStreet().trim(),
                address.getCity().trim(),
                address.getPostalCode().trim());
    }

    @LHTaskMethod("format-shipping-label")
    public String formatShippingLabel(@LHType(isInlineStruct = true) DeliveryAddress address) {
        String label = "%s, %s %s".formatted(address.getStreet(), address.getCity(), address.getPostalCode());
        log.info("Prepared shipping label: {}", label);
        return label;
    }
}
