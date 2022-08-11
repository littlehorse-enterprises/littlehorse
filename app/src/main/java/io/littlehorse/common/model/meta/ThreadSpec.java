package io.littlehorse.common.model.meta;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDatabaseClient;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.wfspec.NodePb;
import io.littlehorse.common.proto.wfspec.NodePb.NodeCase;
import io.littlehorse.common.proto.wfspec.ThreadSpecPb;
import io.littlehorse.common.proto.wfspec.ThreadSpecPbOrBuilder;

public class ThreadSpec extends LHSerializable<ThreadSpecPbOrBuilder> {
    public String name;

    public Map<String, Node> nodes;

    public ThreadSpec() {
        nodes = new HashMap<>();
    }

    @JsonIgnore public Class<ThreadSpecPb> getProtoBaseClass() {
        return ThreadSpecPb.class;
    }

    // Below is Serde
    @JsonIgnore public ThreadSpecPb.Builder toProto() {
        ThreadSpecPb.Builder out = ThreadSpecPb.newBuilder();

        for (Map.Entry<String, Node> e: nodes.entrySet()) {
            out.putNodes(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    public void initFrom(MessageOrBuilder pr) throws LHSerdeError {
        ThreadSpecPbOrBuilder proto = (ThreadSpecPbOrBuilder) pr;
        for (Map.Entry<String, NodePb> p: proto.getNodesMap().entrySet()) {
            Node n = new Node();
            n.name = p.getKey();
            n.threadSpec = this;
            n.initFrom(p.getValue());
            this.nodes.put(p.getKey(), n);
            if (n.type == NodeCase.ENTRYPOINT) {
                this.entrypointNodeName = n.name;
            }
        }
    }

    // Below is Implementation
    @JsonIgnore public String entrypointNodeName;
    @JsonIgnore public WfSpec wfSpec;

    public void validate(LHDatabaseClient dbClient, LHConfig config)
    throws LHValidationError, LHConnectionError {
        if (entrypointNodeName == null) {
            throw new LHValidationError(
                null, "thread " + name + " missing ENTRYPOITNT node!"
            );
        }

        boolean seenEntrypoint = false;
        for (Node node: nodes.values()) {
            if (node.type == NodeCase.ENTRYPOINT) {
                if (seenEntrypoint) {
                    throw new LHValidationError(
                        null,
                        "Thread " + name + " has multiple ENTRYPOINT nodes!"
                    );
                }
                seenEntrypoint = true;
            }
            node.validate(dbClient, config);
        }
    }
}
