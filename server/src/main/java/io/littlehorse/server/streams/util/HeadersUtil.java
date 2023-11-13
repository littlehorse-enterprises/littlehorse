package io.littlehorse.server.streams.util;

import io.littlehorse.common.LHConstants;
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
     * @return TenantId
     * @throws IllegalArgumentException if @{@link LHConstants#TENANT_ID_HEADER_NAME}
     */
    public static String tenantIdFromMetadata(Headers metadata) {
        Header header = metadata.lastHeader(LHConstants.TENANT_ID_HEADER_NAME);
        if (header == null) {
            throw new IllegalArgumentException(LHConstants.TENANT_ID_HEADER_NAME + " header is not present");
        }
        return new String(header.value());
    }

    /**
     * Extract principalId from metadata headers
     * @param metadata Kafka Streams Headers
     * @return PrincipalId
     * @throws IllegalArgumentException if @{@link LHConstants#PRINCIPAL_ID_HEADER_NAME}
     */
    public static String principalIdFromMetadata(Headers metadata) {
        Header header = metadata.lastHeader(LHConstants.PRINCIPAL_ID_HEADER_NAME);
        if (header == null) {
            throw new IllegalArgumentException(LHConstants.PRINCIPAL_ID_HEADER_NAME + " header is not present");
        }
        return new String(header.value());
    }

    /**
     * Build headers metadata for tenant and principal
     * @param tenantId Not null
     * @param principalId Not null
     * @return Kafka Headers
     */
    public static Headers metadataHeadersFor(String tenantId, String principalId) {
        Headers metadata = new RecordHeaders();
        metadata.add(LHConstants.TENANT_ID_HEADER_NAME, tenantId.getBytes());
        metadata.add(LHConstants.PRINCIPAL_ID_HEADER_NAME, principalId.getBytes());
        return metadata;
    }
}
