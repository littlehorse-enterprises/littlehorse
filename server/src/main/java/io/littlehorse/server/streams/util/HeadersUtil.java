package io.littlehorse.server.streams.util;

import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.getable.objectId.PrincipalIdModel;
import io.littlehorse.common.model.getable.objectId.TenantIdModel;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;

/**
 * Statics methods for transforming Kafka/Grpc Headers into
 * tenant  and principal ids
 * Subclasses and Instances are not allowed for this class
 */
public final class HeadersUtil {

    private HeadersUtil() {}

    /**
     * Extract tenantId from metadata headers
     * @param metadata Kafka Streams Headers
     * @return TenantId from metadata or default tenant id if not present
     */
    public static TenantIdModel tenantIdFromMetadata(Headers metadata) {
        Header header = metadata.lastHeader(LHConstants.TENANT_ID_HEADER_NAME);
        if (header == null) {
            return new TenantIdModel(LHConstants.DEFAULT_TENANT);
        }
        return new TenantIdModel(new String(header.value()));
    }

    /**
     * Extract principalId from metadata headers
     * @param metadata Kafka Streams Headers
     * @return PrincipalId
     * @throws IllegalArgumentException if @{@link LHConstants#PRINCIPAL_ID_HEADER_NAME}
     */
    public static PrincipalIdModel principalIdFromMetadata(Headers metadata) {
        Header header = metadata.lastHeader(LHConstants.PRINCIPAL_ID_HEADER_NAME);
        if (header == null) {
            throw new IllegalArgumentException(LHConstants.PRINCIPAL_ID_HEADER_NAME + " header is not present");
        }
        return new PrincipalIdModel(new String(header.value()));
    }

    /**
     * Build headers metadata for tenant and principal
     * @param tenantId Not null
     * @param principalId Not null
     * @return Kafka Headers
     */
    public static Headers metadataHeadersFor(TenantIdModel tenantId, PrincipalIdModel principalId) {
        Headers metadata = new RecordHeaders();
        metadata.add(LHConstants.TENANT_ID_HEADER_NAME, tenantId.toString().getBytes());
        metadata.add(
                LHConstants.PRINCIPAL_ID_HEADER_NAME, principalId.toString().getBytes());
        return metadata;
    }

    public static Headers metadataHeadersFor(String tenantId, String principalId) {
        return metadataHeadersFor(new TenantIdModel(tenantId), new PrincipalIdModel(principalId));
    }
}
