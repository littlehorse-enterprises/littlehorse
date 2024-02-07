package io.littlehorse.server.streams.util;

import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.StoredGetablePb;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetadataCache extends LHCache<String, StoredGetablePb> {

    /*private static final Pattern CACHEABLE_KEY_PATTERN = Pattern.compile(
            "(?<tenant>.+)\\/" + StoreableType.STORED_GETABLE_VALUE + "\\/(?<getableType>\\d+)\\/(?<key>.+)");
    private static final Pattern CACHEABLE_WITHOUT_TENANT_KEY_PATTERN =
            Pattern.compile(StoreableType.STORED_GETABLE_VALUE + "\\/(?<getableType>\\d+)\\/(?<key>.+)");*/

    private static Set<GetableClassEnum> allowedObjets = Set.of(
            GetableClassEnum.TENANT, GetableClassEnum.WF_SPEC, GetableClassEnum.PRINCIPAL, GetableClassEnum.TASK_DEF);

    public MetadataCache() {}

    public void maybeUpdateCache(String storeKey, StoredGetablePb value) throws LHSerdeError {
        /*Matcher keyMatcher = CACHEABLE_KEY_PATTERN.matcher(storeKey);
        Matcher withouTenantKeyMatcher = CACHEABLE_WITHOUT_TENANT_KEY_PATTERN.matcher(storeKey);*/
        /*boolean keyPatternMatches = keyMatcher.matches();
        boolean withoutTenantKeyPatternMatches = withouTenantKeyMatcher.matches();
        boolean isCacheableKey = keyPatternMatches || withoutTenantKeyPatternMatches;*/

        /*if (isCacheableKey) {
            int getableTypeNumber = Integer.parseInt(
                    keyPatternMatches ? keyMatcher.group("getableType") : withouTenantKeyMatcher.group("getableType"));
            GetableClassEnum getableType = GetableClassEnum.forNumber(getableTypeNumber);
            if (getableType != null && allowedObjets.contains(getableType)) {
                evictOrUpdate(value, storeKey);
            }
        }*/
        evictOrUpdate(value, storeKey);
    }

    private void evictOrUpdate(StoredGetablePb value, String cacheKey) throws LHSerdeError {
        if (value == null) {
            super.evictCache(cacheKey);
        } else {
            super.updateCache(cacheKey, value);
        }
    }

    public void maybeCacheMissingKey(String missingKey) {
        if (isCacheableKey(missingKey)) {
            super.updateCache(missingKey, null);
        }
    }

    public void removeMissingKey(String missingKey) {
        if (isCacheableKey(missingKey)) {
            evictCache(missingKey);
        }
    }

    public boolean isCacheableKey(String key) {
        /*Matcher keyMatcher = CACHEABLE_KEY_PATTERN.matcher(key);
        Matcher withouTenantKeyMatcher = CACHEABLE_WITHOUT_TENANT_KEY_PATTERN.matcher(key);
        return keyMatcher.matches() || withouTenantKeyMatcher.matches();*/
        return true;
    }

    // public WfSpecModel getOrCache(String name, Integer version, Supplier<WfSpecModel> cacheable) {
    //     if (version == null) {
    //         version = LATEST_VERSION;
    //     }
    //     WfSpecIdModel cachedId = new WfSpecIdModel(name, version);
    //     return (WfSpecModel) getOrCache(cachedId, cacheable::get);
    // }
}
