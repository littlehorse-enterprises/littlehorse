package io.littlehorse.server.listener;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
}
