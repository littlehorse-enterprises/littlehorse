package io.littlehorse.common.model.getable.global.wfspec.variable;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LHPath;
import io.littlehorse.sdk.common.proto.LHPath.Selector;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public class LHPathModel extends LHSerializable<LHPath> {

    @Getter
    private List<Selector> path;

    @Override
    public LHPath.Builder toProto() {
        LHPath.Builder out = LHPath.newBuilder();
        out.addAllPath(path);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        LHPath p = (LHPath) proto;
        this.path = new ArrayList<>(p.getPathList());
    }

    @Override
    public Class<LHPath> getProtoBaseClass() {
        return LHPath.class;
    }

    public static LHPathModel fromProto(LHPath proto, ExecutionContext context) {
        LHPathModel out = new LHPathModel();
        out.initFrom(proto, context);
        return out;
    }

    public String toJsonStr() {
        StringBuilder pathBuilder = new StringBuilder("$");

        for (Selector selector : path) {
            switch (selector.getSelectorTypeCase()) {
                case INDEX:
                    pathBuilder.append(String.format("[%d]", selector.getIndex()));
                    break;
                case KEY:
                    pathBuilder.append('.');
                    pathBuilder.append(String.format(selector.getKey()));
                    break;
                case SELECTORTYPE_NOT_SET:
            }
        }

        return pathBuilder.toString();
    }
}
