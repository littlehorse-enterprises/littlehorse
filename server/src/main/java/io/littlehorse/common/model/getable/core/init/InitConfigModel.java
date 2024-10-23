package io.littlehorse.common.model.getable.core.init;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.Message;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.proto.InitConfig;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class InitConfigModel extends Storeable<InitConfig> {

    private ServerVersionModel initServerVersion;
    private Date initTime;
    private PrincipalModel initAnonymousPrincipal;
    private TenantModel initDefaultTenant;
    private String pedro;
    public static final String STORE_KEY = "init_config";

    public InitConfigModel() {}
    ;

    public InitConfigModel(
            ServerVersionModel initServerVersion,
            Date initTime,
            PrincipalModel initAnonymousPrincipal,
            TenantModel initDefaultTenant,
            String pedro) {
        this.initServerVersion = initServerVersion;
        this.initTime = initTime;
        this.initAnonymousPrincipal = initAnonymousPrincipal;
        this.initDefaultTenant = initDefaultTenant;
        this.pedro = pedro;
    }

    @Override
    public Builder<InitConfig.Builder> toProto() {
        return InitConfig.newBuilder()
                .setInitVersion(initServerVersion.toProto())
                .setInitTime(LHUtil.fromDate(initTime))
                .setInitAnonymousPrincipal(initAnonymousPrincipal.toProto())
                .setInitDefaultTenant(initDefaultTenant.toProto())
                .setPedro(pedro);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
        InitConfig ic = (InitConfig) proto;
        this.initServerVersion = ServerVersionModel.fromProto(ic.getInitVersion(), ServerVersionModel.class, context);
        this.initTime = LHUtil.fromProtoTs(ic.getInitTime());
        this.initAnonymousPrincipal =
                PrincipalModel.fromProto(ic.getInitAnonymousPrincipal(), PrincipalModel.class, context);
        this.initDefaultTenant = TenantModel.fromProto(ic.getInitDefaultTenant(), TenantModel.class, context);
        this.pedro = ic.getPedro();
    }

    @Override
    public Class<InitConfig> getProtoBaseClass() {
        return InitConfig.class;
    }

    @Override
    public String getStoreKey() {
        return this.STORE_KEY;
    }

    @Override
    public StoreableType getType() {
        return StoreableType.INIT_CONFIG;
    }
}
