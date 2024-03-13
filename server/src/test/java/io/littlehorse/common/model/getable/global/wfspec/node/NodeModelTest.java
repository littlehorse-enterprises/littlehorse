package io.littlehorse.common.model.getable.global.wfspec.node;

import io.littlehorse.TestUtil;
import io.littlehorse.common.exceptions.LHApiException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

public class NodeModelTest {

    private final NodeModel node = spy(new NodeModel());
    private final SubNode mockSubnode = mock();
    private final FailureHandlerDefModel exceptionHandlerDef = TestUtil.exceptionHandler("my-handler");
    private final FailureHandlerDefModel invalidExceptionHandlerDef = TestUtil.exceptionHandler("my.handler");
    @Test
    public void shouldValidateFailureName(){
        doReturn(mockSubnode).when(node).getSubNode();
        node.getFailureHandlers().add(exceptionHandlerDef);
        node.getFailureHandlers().add(invalidExceptionHandlerDef);
        Throwable validationException = Assertions.catchThrowable(node::validate);
        Assertions.assertThat(validationException)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage("FAILED_PRECONDITION: Invalid names for exception handlers: my.handler");
        verify(mockSubnode, never()).validate();
    }


}