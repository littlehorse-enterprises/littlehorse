package io.littlehorse.common.model.getable.core.noderun;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.util.Timestamps;
import io.littlehorse.common.model.AbstractGetableIndexTest;
import io.littlehorse.sdk.common.proto.ExternalEventDefId;
import io.littlehorse.sdk.common.proto.ExternalEventNodeRun;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunId;
import io.littlehorse.sdk.common.proto.TaskNodeRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import org.junit.jupiter.api.Test;

class NodeRunModelIndexTest extends AbstractGetableIndexTest {

    private static final String WF_SPEC_NAME = "my-wf-spec";
    private static final int MAJOR_VERSION = 3;
    private static final int REVISION = 5;
    private static final String EXT_EVT_DEF_NAME = "my-external-event";

    @Override
    public NodeRunModel createGetable() {
        return NodeRunModel.fromProto(createExternalEventNodeRunProto(), NodeRunModel.class, null);
    }

    private NodeRun createExternalEventNodeRunProto() {
        return NodeRun.newBuilder()
                .setId(NodeRunId.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId("test-wf-run").build())
                        .setThreadRunNumber(0)
                        .setPosition(1)
                        .build())
                .setWfSpecId(WfSpecId.newBuilder()
                        .setName(WF_SPEC_NAME)
                        .setMajorVersion(MAJOR_VERSION)
                        .setRevision(REVISION)
                        .build())
                .setStatus(LHStatus.RUNNING)
                .setThreadSpecName("entrypoint")
                .setNodeName("ext-event-node")
                .setArrivalTime(Timestamps.fromMillis(System.currentTimeMillis()))
                .setExternalEvent(ExternalEventNodeRun.newBuilder()
                        .setExternalEventDefId(ExternalEventDefId.newBuilder()
                                .setName(EXT_EVT_DEF_NAME)
                                .build())
                        .build())
                .build();
    }

    @Test
    void shouldReturnDefaultIndexesWhenNotExternalEventNode() {
        NodeRun taskNodeProto = NodeRun.newBuilder()
                .setId(NodeRunId.newBuilder()
                        .setWfRunId(WfRunId.newBuilder().setId("test-wf-run").build())
                        .setThreadRunNumber(0)
                        .setPosition(1)
                        .build())
                .setWfSpecId(WfSpecId.newBuilder()
                        .setName(WF_SPEC_NAME)
                        .setMajorVersion(MAJOR_VERSION)
                        .setRevision(REVISION)
                        .build())
                .setStatus(LHStatus.RUNNING)
                .setThreadSpecName("entrypoint")
                .setNodeName("task-node")
                .setArrivalTime(Timestamps.fromMillis(System.currentTimeMillis()))
                .setTask(TaskNodeRun.newBuilder().build())
                .build();
        NodeRunModel getable = NodeRunModel.fromProto(taskNodeProto, NodeRunModel.class, null);
        assertThat(getable.getIndexEntries()).hasSize(3);
    }

    @Test
    void shouldVerifyStatusAndExtEvtDefNameTag() {
        Tag tag = getTagForKeys("status", "extEvtDefName");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(2);
        assertThat(tag.getAttributes())
                .extracting(Attribute::getEscapedKey)
                .containsExactlyInAnyOrder("status", "extEvtDefName");
        assertThat(tag.getAttributes()).extracting(Attribute::getEscapedVal).contains("RUNNING", EXT_EVT_DEF_NAME);
    }

    @Test
    void shouldVerifyWfSpecNameTag() {
        Tag tag = getTagForKeys("wfSpecName");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(1);
        Attribute attr = tag.getAttributes().get(0);
        assertThat(attr.getEscapedKey()).isEqualTo("wfSpecName");
        assertThat(attr.getEscapedVal()).isEqualTo(WF_SPEC_NAME);
    }

    @Test
    void shouldVerifyWfSpecNameAndMajorVersionTag() {
        Tag tag = getTagForKeys("wfSpecName", "majorVersion");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(2);
        assertThat(tag.getAttributes())
                .extracting(Attribute::getEscapedKey)
                .containsExactlyInAnyOrder("wfSpecName", "majorVersion");
        assertThat(tag.getAttributes())
                .extracting(Attribute::getEscapedVal)
                .contains(WF_SPEC_NAME, String.valueOf(MAJOR_VERSION));
    }

    @Test
    void shouldVerifyWfSpecNameAndMajorVersionAndRevisionTag() {
        Tag tag = getTagForKeys("wfSpecName", "majorVersion", "revision");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(3);
        assertThat(tag.getAttributes())
                .extracting(Attribute::getEscapedKey)
                .containsExactlyInAnyOrder("wfSpecName", "majorVersion", "revision");
        assertThat(tag.getAttributes())
                .extracting(Attribute::getEscapedVal)
                .contains(WF_SPEC_NAME, String.valueOf(MAJOR_VERSION), String.valueOf(REVISION));
    }
}
