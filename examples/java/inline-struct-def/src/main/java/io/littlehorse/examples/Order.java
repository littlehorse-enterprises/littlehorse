package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;

/**
 * A named StructDef that contains an InlineStructDef as one of its fields.
 *
 * <p>The {@code @LHStructDef} annotation registers this class as a named StructDef called
 * {@code order}. The {@code deliveryAddress} field is a plain (unannotated) POJO, so it is
 * serialized as an embedded {@code InlineStructDef} rather than a reference to a separately
 * registered StructDef.
 */
@LHStructDef(value = "order", description = "A customer order with a delivery address.")
public class Order {

    @LHStructField(description = "A unique identifier for this order.")
    private String orderId;

    @LHStructField(description = "The destination address for the shipment.")
    private DeliveryAddress deliveryAddress;

    public Order() {}

    public Order(String orderId, DeliveryAddress deliveryAddress) {
        this.orderId = orderId;
        this.deliveryAddress = deliveryAddress;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }
}
