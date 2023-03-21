package io.littlehorse.server.streamsimpl.searchutils;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import java.util.ArrayList;
import java.util.List;

/**
 * The LHInternalSearch grpc call returns a list of "Full Object Id's", which
 * are the id's of the object but serialized to a String.
 */
public abstract class LHPublicSearchReply<T extends Message>
    extends LHSerializable<T> {

    public byte[] bookmark;
    public LHResponseCodePb code;
    public String message;
    public List<String> objectIds;

    public LHPublicSearchReply() {
        objectIds = new ArrayList<>();
    }
}
