package io.littlehorse.common.model;

/**
 * Cluster Level Commands are NOT namespaced to a Tenant. For example:
 * - PutTenantRequest
 * - PutPrincipalRequest
 * - DeletePrincipalRequest
 * 
 * The above commands operate at the cluster level, not at the Tenant level.
 * 
 * If a SubCommand does not implement the ClusterLevelCommand, then it is
 * considered "namespaced to a Tenant".
 */
public interface ClusterLevelCommand {}
