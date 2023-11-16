package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.NodeMigration;
import io.littlehorse.sdk.common.proto.ThreadSpecMigration;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadSpecMigrationModel extends LHSerializable<ThreadSpecMigration> {

    private String newThreadSpecName;
    private Map<String, NodeMigrationModel> nodeMigrations;

    public Class<ThreadSpecMigration> getProtoBaseClass() {
        return ThreadSpecMigration.class;
    }

    public ThreadSpecMigration.Builder toProto() {
        ThreadSpecMigration.Builder out = ThreadSpecMigration.newBuilder().setNewThreadSpecName(newThreadSpecName);

        for (Map.Entry<String, NodeMigrationModel> entry : nodeMigrations.entrySet()) {
            out.putNodeMigrations(entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    public void initFrom(Message proto) {
        ThreadSpecMigration p = (ThreadSpecMigration) proto;
        newThreadSpecName = p.getNewThreadSpecName();

        for (Map.Entry<String, NodeMigration> e : p.getNodeMigrationsMap().entrySet()) {
            nodeMigrations.put(e.getKey(), LHSerializable.fromProto(e.getValue(), NodeMigrationModel.class));
        }
    }
}
