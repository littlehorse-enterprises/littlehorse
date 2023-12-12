package io.littlehorse.common.model.corecommand.subcommand.job;

public interface BulkJob {

    String processOneRecord();

    void init(String startKey, String endKey);

    boolean hasNext();
}
