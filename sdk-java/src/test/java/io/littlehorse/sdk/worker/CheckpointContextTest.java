package io.littlehorse.sdk.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CheckpointContextTest {
    @Test
    void checkCheckpointContextLogOutput() {
        CheckpointContext context = new CheckpointContext();
        context.log("test log");
        assertEquals("test log", context.getLogOutput());
    }

    @Test
    void checkCheckpointContextLogOutputWithDefaultValue() {
        CheckpointContext context = new CheckpointContext();
        assertEquals("", context.getLogOutput());
    }

    @Test
    void checkCheckpointContextLogOutputWithNullValue() {
        CheckpointContext context = new CheckpointContext();
        context.log(null);
        assertEquals("null", context.getLogOutput());
    }
}
