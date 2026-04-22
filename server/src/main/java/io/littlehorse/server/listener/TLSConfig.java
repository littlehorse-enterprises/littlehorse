package io.littlehorse.server.listener;

import java.io.File;
import java.util.Objects;

public class TLSConfig {

    private final File certChain;
    private final File privateKey;

    public TLSConfig(File certChain, File privateKey) {
        this.certChain = certChain;
        this.privateKey = privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TLSConfig that = (TLSConfig) o;
        return Objects.equals(certChain, that.certChain) && Objects.equals(privateKey, that.privateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certChain, privateKey);
    }

    public File getCertChain() {
        return this.certChain;
    }

    public File getPrivateKey() {
        return this.privateKey;
    }
}
