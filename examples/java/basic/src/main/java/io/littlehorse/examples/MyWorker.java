package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);
    private static final Random RANDOM = new Random();

    @LHTaskMethod(value = "greet", description = "This task greets the user by name.")
    public byte[] greeting(byte[] bytes) {
        byte[] randomBytes = randomBytes();
        if (bytes.length == 0) {
            log.info("Returning a NEW random byte array of length: {}", randomBytes.length);
            return randomBytes;
        }
        log.info("Returning a random byte array of length: {}", bytes.length);
        return concat(bytes, randomBytes);
    }

    private byte[] randomBytes() {
        byte[] randomBytes = new byte[100 * 1024]; // 100 KiB
        RANDOM.nextBytes(randomBytes);
        return randomBytes;
    }
    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
