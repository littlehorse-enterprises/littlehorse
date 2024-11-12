package io.littlehorse.common.model.metadatacommand.subcommand;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.proto.MetadataCommand;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.Workflow;
import io.littlehorse.server.LHServer;
import io.littlehorse.server.TestMetadataManager;
import io.littlehorse.server.streams.ServerTopology;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.processors.MetadataProcessor;
import io.littlehorse.server.streams.util.HeadersUtil;
import io.littlehorse.server.streams.util.MetadataCache;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PutWfSpecRequestModelTest {

    private final KeyValueStore<String, Bytes> nativeMetadataStore = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore(ServerTopology.METADATA_STORE), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();
    private MetadataProcessor metadataProcessor;

    private ExecutionContext executionContext = Mockito.mock();
    private final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext =
            new MockProcessorContext<>();

    private final LHServerConfig config = Mockito.mock(LHServerConfig.class);

    private final LHServer server = Mockito.mock(LHServer.class);

    private final MetadataCache metadataCache = new MetadataCache();
    private final String tenantId = LHConstants.DEFAULT_TENANT;
    private TestMetadataManager metadataManager;

    @BeforeEach
    public void setup() {
        nativeMetadataStore.init(mockProcessorContext.getStateStoreContext(), nativeMetadataStore);
        metadataProcessor = new MetadataProcessor(config, server, metadataCache);
        metadataProcessor.init(mockProcessorContext);
        metadataManager = TestMetadataManager.create(nativeMetadataStore, tenantId, executionContext);
    }

    @Test
    public void shouldTest(){
        Workflow workflow = Workflow.newWorkflow("my-wf", thread -> {
            WfRunVariable requiredVar1 = thread.addVariable("my-var", VariableType.INT).required();
            WfRunVariable requiredVar2 = thread.addVariable("my-var2", VariableType.STR).required();
            thread.execute("my-task", requiredVar1);
            for (int i = 0; i < 100; i++) {
                WfRunVariable loopVar = thread.addVariable("loop-var-"+i, VariableType.INT).required();
                thread.execute("my-task", loopVar);
            }
        });
        TaskDefIdModel taskDefId = new TaskDefIdModel("my-task");
        VariableDefModel var1 = VariableDefModel.fromProto(VariableDef.newBuilder()
                .setName("my-var")
                .setType(VariableType.INT)
                .build(), executionContext);
        VariableDefModel var2 = VariableDefModel.fromProto(VariableDef.newBuilder()
                .setName("my-var")
                .setType(VariableType.INT)
                .build(), executionContext);
        metadataManager.put(new TaskDefModel(taskDefId, List.of(var1)));
        Headers authMetadata = HeadersUtil.metadataHeadersFor(tenantId, "my-principal");
        metadataProcessor.process(new Record<>("mywfspec", new MetadataCommandModel(PutWfSpecRequestModel.fromProto(workflow.compileWorkflow(), executionContext)).toProto().build(), 0L, authMetadata));
        WfSpecModel storedWfSpec = metadataManager.get(new WfSpecIdModel("my-wf", 0, 0));
        Assertions.assertThat(storedWfSpec).isNotNull();
        metadataProcessor.process(new Record<>("mywfspec2", new MetadataCommandModel(PutWfSpecRequestModel.fromProto(workflow.compileWorkflow(), executionContext)).toProto().build(), 0L, authMetadata));
        WfSpecModel sameWfSpec = metadataManager.get(new WfSpecIdModel("my-wf", 0, 1));
        Assertions.assertThat(sameWfSpec).isNull();
    }


}