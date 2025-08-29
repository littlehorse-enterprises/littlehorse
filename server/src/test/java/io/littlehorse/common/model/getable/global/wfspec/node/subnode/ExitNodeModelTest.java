package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import io.littlehorse.TestUtil;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureDefModel;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.server.TestCommandExecutionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ExitNodeModelTest {
    private ExitNodeModel exitNodeModel = new ExitNodeModel();
    private FailureDefModel exception = TestUtil.exceptionFailureDef("my-exception");
    private FailureDefModel invalidException = TestUtil.exceptionFailureDef("my.exception");
    private final PutTenantRequestModel dummySubcommand = new PutTenantRequestModel("my-tenant");
    private final MetadataCommandModel dummyCommand = new MetadataCommandModel(dummySubcommand);
    private TestCommandExecutionContext commandContext =
            TestCommandExecutionContext.create(dummyCommand.toProto().build());

    @Test
    public void shouldValidateFailureName() {
        exitNodeModel.setFailureDef(exception);
        exitNodeModel.validate(commandContext);
        exitNodeModel.setFailureDef(invalidException);
        Throwable throwable = Assertions.catchThrowable(() -> exitNodeModel.validate(commandContext));
        Assertions.assertThat(throwable)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage("INVALID_ARGUMENT: Invalid name for exception: my.exception");
    }
}
