package io.littlehorse.storeinternals;

import static org.mockito.Mockito.mock;

import io.littlehorse.TestUtil;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.model.repartitioncommand.RepartitionCommand;
import io.littlehorse.common.model.repartitioncommand.RepartitionSubCommand;
import io.littlehorse.common.model.repartitioncommand.repartitionsubcommand.CreateRemoteTag;
import io.littlehorse.sdk.common.proto.UserTaskRunStatus;
import io.littlehorse.server.streams.store.LHIterKeyValue;
import io.littlehorse.server.streams.storeinternals.GetableStorageManager;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import io.littlehorse.server.streams.stores.TenantScopedStore;
import io.littlehorse.server.streams.topology.core.CommandProcessorOutput;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.processor.api.MockProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserTaskRunModelStorageManagerTest {

    private final KeyValueStore<String, Bytes> store = Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore("myStore"), Serdes.String(), Serdes.Bytes())
            .withLoggingDisabled()
            .build();

    @Mock
    private LHServerConfig lhConfig;

    private TenantScopedStore localStoreWrapper;

    private String tenantId = "myTenant";

    final MockProcessorContext<String, CommandProcessorOutput> mockProcessorContext = new MockProcessorContext<>();
    private GetableStorageManager getableStorageManager;
    private String wfRunId = "1234567890";

    @Mock
    private ExecutionContext executionContext;

    // private AuthorizationContext testContext = new AuthorizationContextImpl("my-principal-id", tenantId, List.of());

    @BeforeEach
    void setup() {
        initializeDependencies();
        for (UserTaskRunStatus userTaskRunStatus : UserTaskRunStatus.values()) {
            if (userTaskRunStatus == UserTaskRunStatus.UNRECOGNIZED) {
                continue;
            }
            UserTaskRunModel userTaskRun = TestUtil.userTaskRun(wfRunId, mock());
            userTaskRun.setStatus(userTaskRunStatus);
            userTaskRun.setId(new UserTaskRunIdModel(
                    new WfRunIdModel(wfRunId + "1"), UUID.randomUUID().toString()));
            getableStorageManager.put(userTaskRun);
            getableStorageManager.commit();
        }
    }

    private void initializeDependencies() {
        // Commented out due to "UnnecessaryStubbingException";

        // when(mockCoreDao.context()).thenReturn(testContext);
        localStoreWrapper = TenantScopedStore.newInstance(store, new TenantIdModel(tenantId), executionContext);
        getableStorageManager =
                new GetableStorageManager(localStoreWrapper, mockProcessorContext, lhConfig, mock(), executionContext);
        store.init(mockProcessorContext.getStateStoreContext(), store);
    }

    private List<Tag> storedTags() {
        return localTagScan("").map(LHIterKeyValue::getValue).toList();
    }

    private Stream<LHIterKeyValue<Tag>> localTagScan(String keyPrefix) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        localStoreWrapper.prefixScan(keyPrefix, Tag.class), Spliterator.ORDERED),
                false);
    }

    private List<String> storedRemoteTagPrefixStoreKeys() {
        return mockProcessorContext.forwarded().stream()
                .map(MockProcessorContext.CapturedForward::record)
                .map(Record::value)
                .map(CommandProcessorOutput::getPayload)
                .map(lhSerializable -> (RepartitionCommand) lhSerializable)
                .map(RepartitionCommand::getSubCommand)
                .filter(subCommand -> subCommand instanceof CreateRemoteTag)
                .map(RepartitionSubCommand::getPartitionKey)
                .toList();
    }

    private List<String> storedTagPrefixStoreKeys() {
        return storedTags().stream()
                .map(Tag::getStoreKey)
                .map(s -> s.split("/"))
                .map(strings -> strings[0] + "/" + strings[1])
                .toList();
    }

    @Test
    public void indexByUserTaskDefName() {
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__userTaskDefName_ut-name");
    }

    @Test
    public void indexByStatusAndUserTaskDefName_CLAIMED() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__status_ASSIGNED__userTaskDefName_ut-name");
    }

    @Test
    public void indexByStatusAndUserTaskDefName_UNASSIGNED() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__status_UNASSIGNED__userTaskDefName_ut-name");
    }

    @Test
    public void indexByStatusAndUserTaskDefName_ASSIGNED_NOT_CLAIMED() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__status_UNASSIGNED__userTaskDefName_ut-name");
    }

    @Test
    public void indexByUserIdAndUserGroupId() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__userId_33333__userGroup_1234567");
    }

    @Test
    public void indexByStatusAndUserTaskDefName_DONE() {
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__status_DONE__userTaskDefName_ut-name");
    }

    @Test
    public void indexByStatusAndUserTaskDefName_CANCELLED() {
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__status_CANCELLED__userTaskDefName_ut-name");
    }

    @Test
    public void indexByStatus() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__status_ASSIGNED");
    }

    @Test
    public void indexByUserId() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__userId_33333");
    }

    @Test
    public void indexByStatusAndUserId() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__status_ASSIGNED__userId_33333");
    }

    @Test
    public void indexByStatusAndUserId_DONE() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__status_DONE__userId_33333");
    }

    @Test
    public void indexByStatusAndTaskDefNameAndUserId() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys())
                .contains("12/__status_ASSIGNED__userTaskDefName_ut-name__userId_33333");
    }

    @Test
    public void indexByStatusAndTaskDefNameAndUserId_DONE() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys())
                .contains("12/__status_DONE__userTaskDefName_ut-name__userId_33333");
    }

    @Test
    public void indexByStatusAndUserGroup() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__status_ASSIGNED__userGroup_1234567");
    }

    @Test
    public void indexByStatusUserAndTaskDefNameAndUserGroup() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys())
                .contains("12/__status_ASSIGNED__userTaskDefName_ut-name__userGroup_1234567");
    }

    @Test
    public void indexByUserGroup() {
        // Assertions.assertThat(storedRemoteTagPrefixStoreKeys())
        Assertions.assertThat(storedTagPrefixStoreKeys()).contains("12/__userGroup_1234567");
    }
}
