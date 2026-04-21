package io.littlehorse.server.listener;

import java.io.File;

public class TLSConfig {
    private final File certChain;
    private final File privateKey;

    public TLSConfig(File certChain, File privateKey) {
        this.certChain = certChain;
        this.privateKey = privateKey;
    }

    public File getCertChain() {
        return this.certChain;
    }

    public File getPrivateKey() {
        return this.privateKey;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TLSConfig)) return false;
        final TLSConfig other = (TLSConfig) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$certChain = this.getCertChain();
        final Object other$certChain = other.getCertChain();
        if (this$certChain == null ? other$certChain != null : !this$certChain.equals(other$certChain)) return false;
        final Object this$privateKey = this.getPrivateKey();
        final Object other$privateKey = other.getPrivateKey();
        if (this$privateKey == null ? other$privateKey != null : !this$privateKey.equals(other$privateKey))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TLSConfig;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $certChain = this.getCertChain();
        result = result * PRIME + ($certChain == null ? 43 : $certChain.hashCode());
        final Object $privateKey = this.getPrivateKey();
        result = result * PRIME + ($privateKey == null ? 43 : $privateKey.hashCode());
        return result;
    }
}
