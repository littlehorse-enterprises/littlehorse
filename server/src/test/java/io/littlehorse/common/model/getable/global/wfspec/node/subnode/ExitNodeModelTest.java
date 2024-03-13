package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import io.littlehorse.TestUtil;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.node.FailureDefModel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;


class ExitNodeModelTest {
    private ExitNodeModel exitNodeModel = new ExitNodeModel();
    private FailureDefModel exception = TestUtil.exceptionFailureDef("my-exception");
    private FailureDefModel invalidException = TestUtil.exceptionFailureDef("my.exception");

    @Test
    public void shouldValidateFailureName(){
        exitNodeModel.failureDef = exception;
        exitNodeModel.validate();
        exitNodeModel.failureDef = invalidException;
        Throwable throwable = Assertions.catchThrowable(() -> exitNodeModel.validate());
        Assertions.assertThat(throwable)
                .isNotNull()
                .isInstanceOf(LHApiException.class)
                .hasMessage("INVALID_ARGUMENT: Invalid name for exception: my.exception");
    }
}