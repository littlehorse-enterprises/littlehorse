package io.littlehorse.server.streams.storeinternals;

import io.littlehorse.common.model.MetadataGetable;

public interface TenantScopedGetableCallback {

    public void observe(MetadataGetable<?> getable);
}
