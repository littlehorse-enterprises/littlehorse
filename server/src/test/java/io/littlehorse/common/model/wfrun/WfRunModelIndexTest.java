package io.littlehorse.common.model.wfrun;

import static org.assertj.core.api.Assertions.*;

import io.littlehorse.common.model.AbstractGetableIndexTest;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.LHStatus;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.sdk.common.proto.WfSpecId;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.storeinternals.index.Tag;
import org.junit.jupiter.api.Test;

public class WfRunModelIndexTest extends AbstractGetableIndexTest {

    @Override
    public WfRunModel createGetable() {
        return WfRunModel.fromProto(createWfRunProto(), WfRunModel.class, null);
    }

    private WfRun createWfRunProto() {
        return WfRun.newBuilder()
                .setId(WfRunId.newBuilder().setId("test").build())
                .setWfSpecId(WfSpecId.newBuilder()
                        .setName("test-spec")
                        .setMajorVersion(2)
                        .setRevision(1)
                        .build())
                .setStatus(LHStatus.RUNNING)
                .build();
    }

    @Test
    void shouldVerifyWfRunSpecNameTag() {
        Tag tag = getTagForKeys("wfSpecName");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(1);
        Attribute attr = tag.getAttributes().get(0);
        assertThat(attr.getEscapedKey()).isEqualTo("wfSpecName");
        assertThat(attr.getEscapedVal()).isEqualTo("test-spec");
    }

    @Test
    void shouldVerifyWfSpecNameAndStatusTag() {
        Tag tag = getTagForKeys("wfSpecName", "status");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(2);
        assertThat(tag.getAttributes())
                .extracting(Attribute::getEscapedKey)
                .containsExactlyInAnyOrder("wfSpecName", "status");
        assertThat(tag.getAttributes()).extracting(Attribute::getEscapedVal).contains("test-spec", "RUNNING");
    }

    @Test
    void shouldVerifyWfSpecIdTag() {
        Tag tag = getTagForKeys("wfSpecId");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(1);
        Attribute attr = tag.getAttributes().get(0);
        assertThat(attr.getEscapedKey()).isEqualTo("wfSpecId");
        assertThat(attr.getEscapedVal()).isEqualTo("test-spec/00002/00001");
    }

    @Test
    void shouldVerifyMajorVersionTag() {
        Tag tag = getTagForKeys("majorVersion");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(1);
        Attribute attr = tag.getAttributes().get(0);
        assertThat(attr.getEscapedKey()).isEqualTo("majorVersion");
        assertThat(attr.getEscapedVal()).isEqualTo("test-spec/00002");
    }

    @Test
    void shouldVerifyMajorVersionAndStatusTag() {
        Tag tag = getTagForKeys("majorVersion", "status");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(2);
        assertThat(tag.getAttributes())
                .extracting(Attribute::getEscapedKey)
                .containsExactlyInAnyOrder("majorVersion", "status");
        assertThat(tag.getAttributes()).extracting(Attribute::getEscapedVal).contains("test-spec/00002", "RUNNING");
    }

    @Test
    void shouldVerifyWfSpecIdAndStatusTag() {
        Tag tag = getTagForKeys("wfSpecId", "status");
        assertThat(tag).isNotNull();
        assertThat(tag.getAttributes()).hasSize(2);
        assertThat(tag.getAttributes())
                .extracting(Attribute::getEscapedKey)
                .containsExactlyInAnyOrder("wfSpecId", "status");
        assertThat(tag.getAttributes())
                .extracting(Attribute::getEscapedVal)
                .contains("test-spec/00002/00001", "RUNNING");
    }

    @Test
    void shouldIncludeParentWfRunIdTagsWhenParentExists() {
        WfRun proto = WfRun.newBuilder()
                .setId(WfRunId.newBuilder()
                        .setId("child")
                        .setParentWfRunId(WfRunId.newBuilder().setId("parent").build())
                        .build())
                .setWfSpecId(WfSpecId.newBuilder()
                        .setName("child-spec")
                        .setMajorVersion(1)
                        .setRevision(0)
                        .build())
                .setStatus(LHStatus.RUNNING)
                .build();
        WfRunModel getable = WfRunModel.fromProto(proto, WfRunModel.class, null);

        Tag parentTag = getTagForKeys(getable, "parentWfRunId");
        assertThat(parentTag).isNotNull();
        assertThat(parentTag.getAttributes()).hasSize(1);
        assertThat(parentTag.getAttributes().get(0).getEscapedVal()).isEqualTo("parent");

        Tag specAndParentTag = getTagForKeys(getable, "wfSpecName", "parentWfRunId");
        assertThat(specAndParentTag).isNotNull();
        assertThat(specAndParentTag.getAttributes())
                .extracting(Attribute::getEscapedKey)
                .containsExactlyInAnyOrder("wfSpecName", "parentWfRunId");

        Tag statusAndParentTag = getTagForKeys(getable, "status", "parentWfRunId");
        assertThat(statusAndParentTag).isNotNull();
        assertThat(statusAndParentTag.getAttributes())
                .extracting(Attribute::getEscapedKey)
                .containsExactlyInAnyOrder("status", "parentWfRunId");

        Tag specStatusParentTag = getTagForKeys(getable, "wfSpecName", "status", "parentWfRunId");
        assertThat(specStatusParentTag).isNotNull();
        assertThat(specStatusParentTag.getAttributes())
                .extracting(Attribute::getEscapedKey)
                .containsExactlyInAnyOrder("wfSpecName", "status", "parentWfRunId");
    }

    @Test
    void shouldNotIncludeParentWfRunIdTagsWhenNoParent() {
        WfRunModel getable = createGetable();
        Tag parentTag = getTagForKeys(getable, "parentWfRunId");
        assertThat(parentTag).isNull();
    }
}
