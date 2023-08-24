package io.littlehorse.test.internal.step;

import io.littlehorse.test.LHClientTestWrapper;

public interface Step {
    void execute(Object context, LHClientTestWrapper lhClientWrapper);
}
