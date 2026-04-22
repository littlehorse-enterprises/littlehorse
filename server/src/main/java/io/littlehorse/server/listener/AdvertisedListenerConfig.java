package io.littlehorse.server.listener;

import java.util.Objects;

public class AdvertisedListenerConfig {

    private String name;
    private String host;
    private int port;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdvertisedListenerConfig that = (AdvertisedListenerConfig) o;
        return port == that.port && Objects.equals(name, that.name) && Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, host, port);
    }

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

        @java.lang.Override
        public java.lang.String toString() {
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
}
