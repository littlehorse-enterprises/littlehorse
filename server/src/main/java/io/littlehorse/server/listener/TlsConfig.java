package io.littlehorse.server.listener;

import java.io.File;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class TlsConfig {

    private File cert;
    private File key;
}
