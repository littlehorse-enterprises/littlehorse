package io.littlehorse.server.listener;

import java.io.File;
import java.util.Objects;

public class MTLSConfig extends TLSConfig {
    private final File caCertificate;

    public MTLSConfig(File caCertificate, File certChain, File privateKey) {
        super(certChain, privateKey);
        this.caCertificate = caCertificate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MTLSConfig that = (MTLSConfig) o;
        return Objects.equals(caCertificate, that.caCertificate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), caCertificate);
    }

    public File getCaCertificate() {
        return this.caCertificate;
    }
}
