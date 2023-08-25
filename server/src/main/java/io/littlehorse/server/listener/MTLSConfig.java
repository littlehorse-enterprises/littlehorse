package io.littlehorse.server.listener;

import java.io.File;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class MTLSConfig extends TLSConfig {

    private final File caCertificate;

    public MTLSConfig(File caCertificate, File certChain, File privateKey) {
        super(certChain, privateKey);
        this.caCertificate = caCertificate;
    }
}
