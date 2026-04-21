package io.littlehorse.server.listener;

import java.io.File;

public class MTLSConfig extends TLSConfig {
    private final File caCertificate;

    public MTLSConfig(File caCertificate, File certChain, File privateKey) {
        super(certChain, privateKey);
        this.caCertificate = caCertificate;
    }

    public File getCaCertificate() {
        return this.caCertificate;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MTLSConfig)) return false;
        final MTLSConfig other = (MTLSConfig) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        final Object this$caCertificate = this.getCaCertificate();
        final Object other$caCertificate = other.getCaCertificate();
        if (this$caCertificate == null ? other$caCertificate != null : !this$caCertificate.equals(other$caCertificate))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MTLSConfig;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $caCertificate = this.getCaCertificate();
        result = result * PRIME + ($caCertificate == null ? 43 : $caCertificate.hashCode());
        return result;
    }
}
