package io.littlehorse.common.model.getable.global.wfspec.node;

import static org.mockito.Mockito.*;

import io.littlehorse.TestUtil;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.validation.InvalidNodeException;
import io.littlehorse.common.model.metadatacommand.MetadataCommandModel;
import io.littlehorse.common.model.metadatacommand.subcommand.PutTenantRequestModel;
import io.littlehorse.sdk.common.proto.LHErrorType;
import io.littlehorse.server.TestCommandExecutionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class NodeModelTest {

    private final NodeModel node = spy(new NodeModel());
    private final SubNode mockSubnode = mock();
    private final FailureHandlerDefModel exceptionHandlerDef = TestUtil.exceptionHandler("my-handler");
    private final FailureHandlerDefModel invalidExceptionHandlerDef = TestUtil.exceptionHandler("my.handler");
    private final FailureHandlerDefModel technicalErrorHandlerDef =
            TestUtil.exceptionHandler(LHErrorType.TIMEOUT.name());
    private final PutTenantRequestModel dummySubcommand = new PutTenantRequestModel("my-tenant");
    private final MetadataCommandModel dummyCommand = new MetadataCommandModel(dummySubcommand);
    private TestCommandExecutionContext commandContext =
            TestCommandExecutionContext.create(dummyCommand.toProto().build());

    @Test
    public void shouldValidateFailureName() throws InvalidNodeException {
        doReturn(mockSubnode).when(node).getSubNode();
        node.getFailureHandlers().add(exceptionHandlerDef);
        node.getFailureHandlers().add(invalidExceptionHandlerDef);
        Throwable validationException = Assertions.catchThrowable(() -> node.validate(commandContext));
        Assertions.assertThat(validationException)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage("INVALID_ARGUMENT: Invalid names for exception handlers: my.handler");
    }

    @Test
    public void shouldNotValidatePredefinedTechnicalErrors() throws InvalidNodeException {
        doReturn(mockSubnode).when(node).getSubNode();
        node.getFailureHandlers().add(technicalErrorHandlerDef);
        node.validate(commandContext);
        verify(mockSubnode, times(1)).validate(Mockito.any());
    }
}
