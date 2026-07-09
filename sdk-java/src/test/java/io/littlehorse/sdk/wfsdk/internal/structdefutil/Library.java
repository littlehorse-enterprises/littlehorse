package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;
import java.util.Map;

@LHStructDef("library")
public class Library {
    public String name;
    public String[] books = null;
    public int ignoredField;
    public WfRunId maskedField;
    public String stringWithDefault = "hello";
    public String[] lhArrayWithDefault = new String[] {"a", "b"};
    public Map<String, Long> inventory = null;

    public Library() {}

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getBooks() {
        return this.books;
    }

    public void setBooks(String[] books) {
        this.books = books;
    }

    @LHStructIgnore
    public int getIgnoredField() {
        return this.ignoredField;
    }

    public void setIgnoredField(int val) {
        this.ignoredField = val;
    }

    @LHStructField(masked = true)
    public WfRunId getMaskedField() {
        return this.maskedField;
    }

    public void setMaskedField(WfRunId val) {
        this.maskedField = val;
    }

    public String getStringWithDefault() {
        return this.stringWithDefault;
    }

    public void setStringWithDefault(String val) {
        this.stringWithDefault = val;
    }

    public String[] getLhArrayWithDefault() {
        return this.lhArrayWithDefault;
    }

    public void setLhArrayWithDefault(String[] val) {
        this.lhArrayWithDefault = val;
    }

    public Map<String, Long> getInventory() {
        return this.inventory;
    }

    public void setInventory(Map<String, Long> inventory) {
        this.inventory = inventory;
    }
}
