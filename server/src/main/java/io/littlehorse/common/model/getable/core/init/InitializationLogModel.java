package io.littlehorse.common.model.getable.core.init;

import com.google.protobuf.GeneratedMessageV3.Builder;
import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.Storeable;
import io.littlehorse.common.model.getable.global.acl.PrincipalModel;
import io.littlehorse.common.model.getable.global.acl.TenantModel;
import io.littlehorse.common.proto.InitializationLog;
import io.littlehorse.common.proto.StoreableType;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.PrincipalId;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.sdk.common.proto.Tenant;
import io.littlehorse.sdk.common.proto.TenantId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class InitializationLogModel extends Storeable<InitializationLog> {

    private ServerVersionModel initServerVersion;
    private Date initTime;
    private PrincipalModel initAnonymousPrincipal;
    private TenantModel initDefaultTenant;
    public static final String SERVER_INITIALIZED_KEY = "server_initialized";

    public InitializationLogModel() {}

    public InitializationLogModel(
            ServerVersionModel initServerVersion,
            Date initTime,
            PrincipalModel initAnonymousPrincipal,
            TenantModel initDefaultTenant,
            String obiWan) {
        this.initServerVersion = initServerVersion;
        this.initTime = initTime;
        this.initAnonymousPrincipal = initAnonymousPrincipal;
        this.initDefaultTenant = initDefaultTenant;
    }

    @Override
    public Builder<InitializationLog.Builder> toProto() {
        return InitializationLog.newBuilder()
                .setInitVersion(initServerVersion.toProto())
                .setInitTime(LHUtil.fromDate(initTime))
                .setInitAnonymousPrincipal(initAnonymousPrincipal.toProto())
                .setInitDefaultTenant(initDefaultTenant.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        InitializationLog il = (InitializationLog) proto;
        this.initServerVersion = ServerVersionModel.fromProto(il.getInitVersion(), ServerVersionModel.class, context);
        this.initTime = LHUtil.fromProtoTs(il.getInitTime());
        this.initAnonymousPrincipal =
                PrincipalModel.fromProto(il.getInitAnonymousPrincipal(), PrincipalModel.class, context);
        this.initDefaultTenant = TenantModel.fromProto(il.getInitDefaultTenant(), TenantModel.class, context);
    }

    @Override
    public Class<InitializationLog> getProtoBaseClass() {
        return InitializationLog.class;
    }

    @Override
    public String getStoreKey() {
        return SERVER_INITIALIZED_KEY;
    }

    @Override
    public StoreableType getType() {
        return StoreableType.INITIALIZATION_LOG;
    }

    public static PrincipalModel getAnonymousPrincipalModel(ExecutionContext context) {
        Principal anonymousPrincipal = Principal.newBuilder()
                .setId(PrincipalId.newBuilder().setId(LHConstants.ANONYMOUS_PRINCIPAL))
                .setGlobalAcls(ServerACLs.newBuilder().addAcls(LHConstants.ADMIN_ACL))
                .build();

        return PrincipalModel.fromProto(anonymousPrincipal, PrincipalModel.class, context);
    }

    public static TenantModel getDefaultTenantModel(ExecutionContext context) {
        Tenant defaultTenant = Tenant.newBuilder()
                .setId(TenantId.newBuilder().setId(LHConstants.DEFAULT_TENANT))
                .build();

        return TenantModel.fromProto(defaultTenant, TenantModel.class, context);
    }
}
