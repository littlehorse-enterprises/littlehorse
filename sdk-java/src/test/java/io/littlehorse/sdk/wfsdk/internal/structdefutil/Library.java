package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;

@LHStructDef("library")
public class Library {
    public String name;
    public String[] books = null;
    public int ignoredField;
    public WfRunId maskedField;
    public String stringWithDefault = "hello";

    @LHStructField(isLHArray = true)
    public String[] lhArrayWithDefault = new String[] {"a", "b"};

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
}
