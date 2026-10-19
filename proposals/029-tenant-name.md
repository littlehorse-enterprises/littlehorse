# Tenant Name

Author: Eduwer Camacaro


## Motivation

A Tenant currently has an ID but no separate human-readable name. The ID identifies the tenant in API requests, client configuration, and authorization rules. It also has naming restrictions: it must be a valid hostname and cannot start with a digit.

Using that same value as the tenant's name couples identity to presentation. Users should be able to give a tenant a descriptive name and change that name as their organization evolves without changing client configuration or authorization rules.

This proposal adds an optional `name` to Tenant.

For example, a tenant with ID `acme-prod` could have the name `Acme — Production`. Renaming it to `Acme — US Production` would preserve its identity and all existing references.

## Proposed Protocol Buffer Changes

In `schemas/littlehorse/acls.proto`, add fields to the existing messages:

```protobuf
message Tenant {
    TenantId id = 1;
    google.protobuf.Timestamp created_at = 2;
    optional OutputTopicConfig output_topic_config = 3;

    // A mutable, human-readable name. It does not identify the Tenant.
    optional string name = 4;
}

message PutTenantRequest {
    string id = 1;
    optional OutputTopicConfig output_topic_config = 2;

    // If omitted, preserves the existing name.
    // An empty string clears the name.
    optional string name = 3;
}
```

In `schemas/littlehorse/service.proto`, add an optional name filter:

```protobuf
message SearchTenantRequest {
    optional int32 limit = 1;
    optional bytes bookmark = 2;

    // Return tenants whose name exactly matches this string.
    optional string name = 3;
}
```

No changes are required to `TenantId`, `TenantIdList`, or the RPC signatures.

## Semantics

- The tenant ID remains unique within the cluster and stable throughout the tenant's lifetime. This proposal does not add an operation to change it.
- The name is optional, mutable, and does not need to be unique. Multiple tenants may share a name.
- The optional `SearchTenantRequest.name` filter matches the complete name exactly and is case-sensitive. All tenants with that name are eligible to be returned, subject to pagination.
- A nonempty name containing only whitespace is rejected with `INVALID_ARGUMENT`. Other nonempty names are stored as supplied.
- The name is presentation metadata. API lookups, tenant selection in client configuration, ACLs, storage keys, and output topic naming continue to use the tenant ID.
- Renaming a tenant uses `PutTenant` with its existing ID and the new name, subject to the existing authorization requirements for that operation.

## SDK, CLI, and Dashboard Changes

Regenerate protobuf code for all SDKs to expose the new fields and their presence checks. No new workflow builder APIs are required.

Add an optional `--name` flag to the `lhctl` tenant put command. Omitting the flag preserves the current name, while supplying an empty string clears it. Tenant get output includes the new field when present.

The dashboard should display `name` when available and fall back to the tenant ID otherwise. Tenant selection should show the ID alongside the name to distinguish tenants with duplicate names. Selected tenant values and requests continue to use IDs.

Expose the optional name filter in the `lhctl` tenant search command so clients can list tenants whose name exactly matches the supplied value.

Uniqueness enforcement and addressing tenants by name are outside the scope of this proposal.


## Backwards Compatibility

The protobuf changes are additive and preserve all existing field numbers. Existing tenants deserialize without a name, and existing clients can continue to create and address tenants using IDs.

When an older client sends a `PutTenant` request to an upgraded server, the request omits `name`. The server must preserve the name already stored for that tenant, if any. If the tenant has no stored name, it remains unset. Creating a tenant without this field also remains supported.

There are no API deprecations or changes to tenant ID validation.
