package io.littlehorse.canary.metronome;

import io.littlehorse.canary.metronome.internal.LocalRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetronomeGetWfExecuter {
    public MetronomeGetWfExecuter(final LocalRepository repository) {
        log.info("GetWf Metronome Started");
    }
}
