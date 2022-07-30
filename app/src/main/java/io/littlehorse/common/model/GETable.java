package io.littlehorse.common.model;

import java.util.Date;
import com.google.protobuf.MessageOrBuilder;

public abstract class GETable<T extends MessageOrBuilder> extends LHSerializable<T> {
    public String id;
    public Date createdAt;
}

/*
 * Some random thoughts:
 * - each GETable has a partition key and an ID. They may be different.
 * - For example, we want TaskRun's for a WfRun to end up on the same host
 * - VariableValue's for a ThreadRun will end up on the same node as each other
 * - Will we query VariableValue's from the Scheduler topology or from the
 *   API topology?
 * 
 * Will we make it possible to deploy the Scheduler separately from the API?
 *   - yes we will.
 */