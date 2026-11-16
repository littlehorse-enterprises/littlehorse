package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;

@LHStructDef(value = "item-request-form", description = "empty")
public class ItemRequestForm {

    @LHStructField(description = "The item being requested.")
    public String requestedItem;

    @LHStructField(description = "The justification for requesting the item.")
    public String justification;

    public ItemRequestForm() {}

    public ItemRequestForm(String requestedItem, String justification) {
        this.requestedItem = requestedItem;
        this.justification = justification;
    }

    public String getRequestedItem() {
        return requestedItem;
    }

    public String getJustification() {
        return justification;
    }

    public void setRequestedItem(String requestedItem) {
        this.requestedItem = requestedItem;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
