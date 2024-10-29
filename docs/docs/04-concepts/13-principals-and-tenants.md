# Authz, `Principals`, and `Tenants`

In order to provide a secure platform for use at large enterprises, LittleHorse supports multi-tenancy natively. This is accomplished through two API Objects:

* The `Principal`, which represents the identity of a client of LittleHorse (either a human or a machine).
* The `Tenant`, which represents a logically isolated environment within LittleHorse.

`Principal`s and `Tenant`s reached General Availability in the `0.10.x` release of LittleHorse.

## Authz with `Principal`s

In the 2020's, every company whether large or small must treat security as a top priority. This is especially true for systems like LittleHorse which can be used to orchestrate your most critical business processes. Therefore, when designing LittleHorse, we introduced the `Principal` concept to represent the Identity of a user (machine or human) of LittleHorse.

Design goals for `Principal`s include:
* The Principle of Least Privilege.
* Fine-grained access control.
* Reliance upon open standards.

A [`Principal`](../08-api.md#principal) is a first-class object in the LittleHorse API which can be adminstered through [various](../08-api.md#putprincipal) [rpc](../08-api.md#whoami) [requests](../08-api.md#deleteprincipal).

### Authenticating a Caller

Every RPC call to the LittleHorse API is resolved to a `Principal` via authentication. The LittleHorse Server supports both OAuth and MTLS for authentication, configured on a per-listener basis.

The rules for authenticating a call are as follows:

* If the listener uses `MTLS`:
  * The `Principal` ID is determined from the `Common Name` on the client's certificate.
  * Any requests that fail to present a valid certificate signed by a configured trusted authority will fail at the network level.
* If the listener uses `OAUTH`:
  * For requests from a human client (eg. `lhctl` and the Dashboard), the `Principal` ID is determined by the User ID of the presented token.
  * For requests from a machine client (eg. a Task Worker), the `Principal` ID is determined by the Client ID of the presented token.
  * Requests that fail to present a valid token from the configured issuer fail with `UNAUTHENTICATED`.

If a `Principal` exists with a matching ID, the request is authorized according to that `Principal`'s ACL's. If no `Principal` exists with the provided ID, then the request is authorized according to the `anonymous` `Prncipal`'s ACL's.

<hr/>

## Initial Resource Configurations

When a LittleHorse Cluster is created for the first time, there exists one `Tenant` named `default` and one `Principal` named `anonymous`. These resources are provided with initial configurations out-of-the-box to facilitate a quick and easy setup for your new cluster.

This is great for getting started in local development, and also for ease of use on private and trusted networks. However, LittleHorse allows you to fully secure your cluster by changing the initial configurations of the `anonymous` `Principal`.

There are several motivations for the out-of-the-box design:

* **Online Migrations to Secured Clusters**: this design allows users to migrate to a secure cluster without downtime by adding a secured listener, adding `Principal`s for each application, moving applications to the new listener, and disabling `anonymous`'s permissions.
* **Developer Experience**: this design allows LittleHorse to "just work" without understanding `Principal`s or `Tenant`s.
* **Fine-Grained Permissions**: ACL's on the `Principal` resource allow fine-grained control over who can access what resource.

### The `default` Tenant

An authenticated request made without a Tenant ID is designated for the `default` `Tenant`. Alternatively, requests made using an unknown Tenant ID will be rejected.

### Initial Configuration

The `default` `Tenant` is initialized simply as a `Tenant` with the ID `default`. If `Tenant`s receive more metadata in the future, we will update the configuration here.

Represented as a `Tenant protobuf object`(../08-api.md#tenant), the initial `default` `Tenant` configuration looks like this:

```proto
{
  "id": "default"
}
```

<hr/>

### The `anonymous` Principal

An authenticated request made with an unknown Principal ID is authorized with the `anonymous` `Principal`. 

#### Initial Configuration

The `anonymous` `Principal` is initialized with **full admin privileges** over the *entire* LittleHorse cluster.

Represented as a [Principal protobuf object](../08-api.md#principal), the initial `anonymous` `Principal` configuration looks like this:

```proto
{
    "global_acls": {
        "acls": [
            {
                "resources": [
                    "ALL_RESOURCES"
                ],
                "allowed_actions": [
                    "ALL_ACTIONS"
                ]
            }
        ]
    },
    "id": "anonymous"
}
```

:::danger
The `anonymous` `Principal` should **ALWAYS** be demoted before your LittleHorse Cluster is used in production.
:::


#### Updating the Permissions

Like any other `Principal`, the `anonymous` Principal's permissions can be overwritten via a `PutPrincipal` request from a caller with proper permissions and the `overwrite` parameter set to true.

#### Deletion

Unlike other `Principal`s, the `anonymous` `Principal` **cannot** be deleted, as it represents all cases where a client sends an authenticated request with an unknown Principal ID.

<hr/>

### Authorizing Requests

Once a request has been authenticated and a `Principal` has been determined (either as the `anonymous` `Principal` or a custom user-created `Principal`), the LH Cluster checks the ACL's on the `Principal` against the required permissions for the request.

The [ACL resources](../08-api.md#aclresource) and [actions](../08-api.md#aclaction) over those resources are documented in our API Specification. Many requests are scoped to a `Tenant`; in those cases, the LH Cluster verifies that the calling `Principal` has the appropriate permissions either globally or within the specified `Tenant`.

If a calling `Principal` lacks permissions to perform the request, then the GRPC error `PERMISSION_DENIED` is returned.

## Multi-Tenancy with `Tenants`

At large enterprises, there is sometimes a motivation to provide logically isolated environments within a single physical LittleHorse cluster. This can be achieved using a `Tenant`.

Every workflow-related resource (`WfSpec`, `WfRun`, `TaskDef`, `TaskRun`, `NodeRun`, `ExternalEvent`, etc) is scoped to within a `Tenant`. That means that within tenant `foo` and tenant `bar`, you can have two different `WfSpec`'s titled `my-workflow`.

A `Principal` can have `global_acls` assigned to it, which allow the `Principal` to perform actions over any `Tenant`. Additionally, a `Principal` may have `per_tenant_acls` assigned to it, which allow the `Principal` perform actions over resources within only a specific and specified `Tenant`.

Clients can control which `Tenant` they are connected to by using the `LHC_TENANT_ID` configuration.
