package io.littlehorse.server.listener;

public class AdvertisedListenerConfig {
    private String name;
    private String host;
    private int port;

    AdvertisedListenerConfig(final String name, final String host, final int port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public static class AdvertisedListenerConfigBuilder {
        private String name;
        private String host;
        private int port;

        AdvertisedListenerConfigBuilder() {}

        /**
         * @return {@code this}.
         */
        public AdvertisedListenerConfig.AdvertisedListenerConfigBuilder name(final String name) {
            this.name = name;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AdvertisedListenerConfig.AdvertisedListenerConfigBuilder host(final String host) {
            this.host = host;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public AdvertisedListenerConfig.AdvertisedListenerConfigBuilder port(final int port) {
            this.port = port;
            return this;
        }

        public AdvertisedListenerConfig build() {
            return new AdvertisedListenerConfig(this.name, this.host, this.port);
        }

        @Override
        public String toString() {
            return "AdvertisedListenerConfig.AdvertisedListenerConfigBuilder(name=" + this.name + ", host=" + this.host
                    + ", port=" + this.port + ")";
        }
    }

    public static AdvertisedListenerConfig.AdvertisedListenerConfigBuilder builder() {
        return new AdvertisedListenerConfig.AdvertisedListenerConfigBuilder();
    }

    public String getName() {
        return this.name;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof AdvertisedListenerConfig)) return false;
        final AdvertisedListenerConfig other = (AdvertisedListenerConfig) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.getPort() != other.getPort()) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$host = this.getHost();
        final Object other$host = other.getHost();
        if (this$host == null ? other$host != null : !this$host.equals(other$host)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof AdvertisedListenerConfig;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPort();
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $host = this.getHost();
        result = result * PRIME + ($host == null ? 43 : $host.hashCode());
        return result;
    }
}
