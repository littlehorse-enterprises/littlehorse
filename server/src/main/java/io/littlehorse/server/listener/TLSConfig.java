package io.littlehorse.server.listener;

import java.io.File;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class TLSConfig {

    private final File certChain;
    private final File privateKey;

    public TLSConfig(File certChain, File privateKey) {
        this.certChain = certChain;
        this.privateKey = privateKey;
    }
}
