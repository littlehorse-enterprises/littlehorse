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
- A nonempty name containing only whitespace or any name containing `/` is rejected with `INVALID_ARGUMENT`. Other nonempty names are stored as supplied.
- The name is presentation metadata. API lookups, tenant selection in client configuration, ACLs, storage keys, and output topic naming continue to use the tenant ID.
- Renaming a tenant uses `PutTenant` with its existing ID and the new name, subject to the existing authorization requirements for that operation.
- At the API level, omitting `name` preserves the existing name, while an empty string clears it. Omitting `output_topic_config` disables output; supplying it replaces the configuration.

## SDK, CLI, and Dashboard Changes

Regenerate protobuf code for all SDKs to expose the new fields and their presence checks. No new workflow builder APIs are required.

Add an optional `--name` flag to the `lhctl` tenant put command. Each invocation creates or updates the tenant using the supplied name and output settings. Users must retype every setting they want to keep. Omitting `--name` or supplying an empty string clears the display name: the CLI explicitly sends an empty name rather than omitting the API field. Tenant get output includes the new field when present.

Output settings are selected as follows:

- `--all-entity-events` enables output for all eligible entities.
- `--no-entity-events` enables output for explicitly enabled entities only; it does not disable the output topic.
- Omitting both output modes disables output. There is no separate disable flag.
- A boolean flag set to `false` does not select that mode. Selecting both modes as `true` is rejected before confirmation or any request.

Every tenant put displays the submitted settings and requires confirmation before sending the request, including when the tenant may be new. Property labels appear in bold in the terminal. For example:

```text
$ lhctl put tenant obiwan --name "Obi-wan Kenobi" --no-entity-events
Apply the following tenant settings?

  Tenant ID: "obiwan"
  Display Name: "Obi-wan Kenobi"
  Output Topic Configuration: Enabled — explicitly enabled entities only

This will overwrite these settings if the tenant already exists.
Continue? [y/N] y
```

The user must enter `y` or `yes` (case-insensitive). Empty input, any other answer, or an input read error aborts without writing. A cleared name is displayed as `(none; clears any existing display name)` and omitted output settings as `Disabled`. After confirmation, the CLI sends the request and prints the returned tenant, or reports the request error.

The CLI does not fetch or merge existing settings. Confirmation does not lock the tenant or perform a conditional update: a concurrent write can still be overwritten. This intentionally replaces the earlier fetch-and-preserve approach, which could replay stale output settings. No additional protobuf fields are needed for confirmation or disabling output.

The dashboard should display `name` when available and fall back to the tenant ID otherwise. Tenant selection should show the ID alongside the name to distinguish tenants with duplicate names. Selected tenant values and requests continue to use IDs.

The dashboard's tenant selector filters by case-insensitive substring matches against either the name or ID. This local filtering differs from the API's exact-name search.

Expose the optional name filter in the `lhctl` tenant search command so clients can list tenants whose name exactly matches the supplied value.

Uniqueness enforcement and addressing tenants by name are outside the scope of this proposal.


## Backwards Compatibility

The protobuf changes are additive and preserve all existing field numbers. Existing tenants deserialize without a name, and existing clients can continue to create and address tenants using IDs.

When an older client sends a `PutTenant` request to an upgraded server, the request omits `name`. The server must preserve the name already stored for that tenant, if any. If the tenant has no stored name, it remains unset. Creating a tenant without this field also remains supported.

There are no API deprecations or changes to tenant ID validation.

The CLI intentionally requires confirmation on every tenant put, so scripts must provide an affirmative answer through standard input. CLI omission of `--name` clears the name even though omission of the API field preserves it; the CLI sends an explicit empty string to implement this behavior.
