package io.littlehorse.server.listener;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class AdvertisedListenerConfig {

    private String name;
    private String host;
    private int port;
}
