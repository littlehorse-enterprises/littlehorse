package io.littlehorse.server.quotas;

import io.littlehorse.common.model.getable.global.acl.QuotaModel;
import lombok.Getter;

@Getter
class StateHandle {
    private final String key;
    private final QuotaModel quota;
    private final QuotaState state;

    StateHandle(QuotaModel quota, QuotaState state) {
        this.key = quota.getObjectId().toString();
        this.quota = quota;
        this.state = state;
    }
}
