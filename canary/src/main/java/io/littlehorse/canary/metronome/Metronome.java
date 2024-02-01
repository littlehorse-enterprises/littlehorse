package io.littlehorse.canary.metronome;

import io.littlehorse.canary.kafka.MetricsEmitter;
import java.io.Closeable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Metronome extends TimerTask implements Closeable {

    private final MetricsEmitter emitter;
    private final Timer executor;

    public Metronome(final MetricsEmitter emitter) {
        this.emitter = emitter;
        executor = new Timer(true);
        executor.schedule(this, 0, TimeUnit.SECONDS.toMillis(1));
    }

    @Override
    public void run() {
        log.trace("Executing metronome");
    }

    @Override
    public void close() {
        executor.cancel();
    }
}
