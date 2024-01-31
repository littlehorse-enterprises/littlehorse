package io.littlehorse.test;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.sdk.common.proto.ACLAction;
import io.littlehorse.sdk.common.proto.ACLResource;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.Principal;
import io.littlehorse.sdk.common.proto.PutPrincipalRequest;
import io.littlehorse.sdk.common.proto.PutTenantRequest;
import io.littlehorse.sdk.common.proto.ServerACL;
import io.littlehorse.sdk.common.proto.ServerACLs;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.worker.LHTaskWorker;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import io.littlehorse.test.exception.LHTestInitializationException;
import io.littlehorse.test.internal.StandaloneTestBootstrapper;
import io.littlehorse.test.internal.TestContext;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class LHExtension implements BeforeAllCallback, TestInstancePostProcessor {

    private static final ExtensionContext.Namespace LH_TEST_NAMESPACE =
            ExtensionContext.Namespace.create(LHExtension.class);
    private static final String LH_TEST_CONTEXT = "LH-test-context";

    @Override
    public void beforeAll(ExtensionContext context) {
        Awaitility.setDefaultPollInterval(Duration.of(50, ChronoUnit.MILLIS));
        Awaitility.setDefaultTimeout(Duration.of(1000, ChronoUnit.MILLIS));
        getStore(context)
                .getOrComputeIfAbsent(
                        LH_TEST_CONTEXT,
                        s -> new TestContext(new StandaloneTestBootstrapper(new PrincipalIdModel(getPrincipalId()))),
                        TestContext.class);
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(LH_TEST_NAMESPACE);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        ExtensionContext.Store store = getStore(context);
        TestContext testContext = store.get(LH_TEST_CONTEXT, TestContext.class);
        maybeCreateTenantAndPrincipal(testContext);
        try {
            List<LHTaskWorker> workers = testContext.discoverTaskWorkers(testInstance);
            for (LHTaskWorker worker : workers) {
                if (store.get(worker.getTaskDefName()) != null) {
                    continue;
                }
                store.put(worker.getTaskDefName(), worker);
                worker.registerTaskDef();
                TaskDefId taskDefId =
                        TaskDefId.newBuilder().setName(worker.getTaskDefName()).build();
                Awaitility.await()
                        .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                        .until(() -> testContext.getLhClient().getTaskDef(taskDefId), taskDef -> taskDef != null);
                Awaitility.await().until(() -> {
                    try {
                        worker.start();
                        return true;
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
            }
            testContext.registerUserTaskSchemas(testInstance);
            List<ExternalEventDef> externalEventDefinitions =
                    testContext.discoverExternalEventDefinitions(testInstance);
            externalEventDefinitions.forEach(testContext::registerExternalEventDef);
        } catch (IOException | IllegalAccessException e) {
            throw new LHTestInitializationException("Something went wrong registering task workers", e);
        }
        testContext.instrument(testInstance);
    }

    private void maybeCreateTenantAndPrincipal(TestContext testContext) {
        String principalId = getPrincipalId();
        try {
            testContext
                    .getAnonymousClient()
                    .putTenant(PutTenantRequest.newBuilder()
                            .setId(testContext.getConfig().getTenantId())
                            .build());
            ServerACLs acls = ServerACLs.newBuilder()
                    .addAcls(ServerACL.newBuilder()
                            .addAllowedActions(ACLAction.ALL_ACTIONS)
                            .addResources(ACLResource.ACL_ALL_RESOURCES)
                            .build())
                    .build();
            testContext
                    .getAnonymousClient()
                    .putPrincipal(PutPrincipalRequest.newBuilder()
                            .setId(principalId)
                            .setGlobalAcls(acls)
                            .putPerTenantAcls(testContext.getConfig().getTenantId(), acls)
                            .build());
            // wait until the principal is propagated into the server
            Awaitility.await()
                    .atMost(Duration.ofSeconds(15))
                    .ignoreException(RuntimeException.class)
                    .until(() -> {
                        Principal whoami = testContext.getLhClient().whoami(Empty.getDefaultInstance());
                        return whoami.getId().getId().equals(principalId);
                    });
        } catch (StatusRuntimeException ex) {
            if (!ex.getStatus().getCode().equals(Status.Code.ALREADY_EXISTS)) {
                throw ex;
            }
        }
    }

    private String getPrincipalId() {
        return System.getenv().getOrDefault("LH_CLIENT_ID", "tyler");
    }
}
