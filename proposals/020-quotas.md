# Request Quotas

This proposal aims to support rudimentary request quotas for write requests:

* Introduce a `Quota` Getable at the Tenant level, which may govern all `Principal`s or one specific `Principal`.
* The first dimension governed by a `Quota` is mutating unary GRPC calls (non-streaming Write requests).
* `Quota`s are specified at a cluster-wide level and enforced via approximation at a per-server level.
* Introduce a `QuotaUsageWindow` which is designed similarly to workflow metrics in order to allow users to track quota utilization.

## Public API's

### Administering Quotas

We'll introduce a `Quota` object as a Tenant-scoped `Getable`. (For reasoning on why not Cluster-scoped, see the rejected alterantives).

```proto
// A Quota defines limits for resources used by a certain `Principal` or all `Principal`s
// in a certain `Tenant`.
//
// Note that Quotas are enforced per-server instance.
message Quota {
    // The Id of the `Quota`
    QuotaId id = 1;

    // This field controls the number of mutating (write) unary GRPC calls made
    // per second in the cluster.
    int32 write_requests_per_second = 2;
}

// Identifies a `Quota`
message QuotaId {
    // The governed `Tenant`
    TenantId tenant = 1;

    // If not set, the quota applies to all `Principal`s in the `Tenant`.
    optional PrincipalId principal = 2;
}

// Creates or Updates a `Quota`
message PutQuotaRequest {
    // The governed `Tenant`
    TenantId tenant = 1;

    // If not set, the quota applies to all `Principal`s in the `Tenant`.
    optional PrincipalId principal = 2;

    // This field controls the number of mutating (write) unary GRPC calls made
    // per second in the cluster.
    int32 write_requests_per_second = 3;
}

// Deletes a `Quota`
message DeleteQuotaRequest {
    // The `Quota` to delete.
    QuotaId id = 1;
}

message SearchQuotaRequest {

}

// Creates or Updates a Quota
rpc PutQuota(PutQuotaRequest) returns(Quota) {}

// Gets a Quota
rpc GetQuota(QuotaId) returns(Quota) {}

// Deletes a Quota
rpc DeleteQuota(DeleteQuotaRequest) returns(google.protobuf.Empty) {}
```

#### `lhctl`

```
lhctl put quota <tenantId> --principal <principalId> --writeRequestsPerSecond <reqPerSecond>
lhctl get quota <tenantId> --principal <principalId>
lhctl search quota --tenantId <tenantId>
```

Example:

```
lhctl put quota my-tenant --principal obi-wan --writeRequestsPerSecond 100
lhctl get quota my-tenant
lhctl get quota my-tenant --principal obi-wan
```

#### ACL's Required

Because the `Quota` is a Cluster-scoped Getable (i.e. not accessed from within the Tenant but rather next ot `Tenant`s and `Principal`s in the store), to interact with it you must have _**global**_ `AclAction.WRITE_METADATA` over the resource `AclResource.ACL_QUOTA` (which is added in this proposal).

### Quotas Exceeded Path

When a quota is exceeded, the server will respond with `RESOURCE_EXHAUSTED`. The server will respond with a `com.google.protobuf.RetryInfo` including information about how long to delay the retry. We'll edit the `LHConfig` in all five SDK's to include interceptors which transparently retry on `RESOURCE_EXHAUSTED` after the specified delay (see implementation details below).

[Here](https://github.com/grpc/grpc-java/issues/8899) and [here](https://grpc.io/docs/guides/retry/) is some background information that helped motivate this design.

### Tracking Quotas

We'll allow users to track quota utilization. Importantly, applications within a `Tenant` will need to track their own quotas. To do this, we'll introduce metrics-style quota utilization windows which are aggregated every minute, just like other utilization metrics:

```proto
// A QuotaUsageWindow tracks the utilization of a quota over a one-minute period.
message QuotaUsageWindow {
    // The id contains information about when the window started.
    QuotaUsageWindowId id = 1;

    // Total incoming requests received during this window, whether accepted
    // or throttled..
    int32 requests_observed = 2;

    // Total requests throttled during this window.
    int32 requests_throttled = 3;

    // Total time that requests were throttled during this window.
    int64 total_throttle_time_ms = 4;
}

// Id of a QuotaUsageWindow
message QuotaUsageWindowId {
    // The ID of the quota being tracked.
    QuotaId quota_id = 1;

    // The start time of the window being tracked.
    google.protobuf.Timestamp start_time = 2;
}

// Request to list quota usage windows.
message ListQuotaUsageWindowRequest {
    // The id of the quota to inspect.
    QuotaId quota_id = 1;

    // The time of the last window (most recent) to look at.
    google.protobuf.Timestamp last_window = 2;

    // Number of windows to load
    int32 num_windows = 3;
}

rpc ListQuotaUsageWindows(ListQuotaUsageWindowRequest) returns (QuotaUsageWindowList) {}
rpc GetQuotaUsageWindow(QuotaUsageWindowId) returns (QuotaUsageWindow) {}
```

Quota tracking is at most once: if a server crashes before the window closes, the data is not forwarded. This is because the quotas are calculated before sending a `Command` into Kafka (more details below in implementation section).

## Design & Implementation

We are a GRPC service, so we'll do our best to be as GRPC-native as possible.

We'll add logic in the GRPC server to load the `Quota`(s) affected by a request, increment them, and maybe reject the request according to the quota policy. Rejected requests receive `RESOURCE_EXHAUSTED` along with a `RetryInfo` specifying when to retry.

The requests will be rejected _before_ sending the `Command` into Kafka, making it safe (idempotent) to retry.

### Client Interceptors

Every SDK's `LHConfig` will be edited to put client interceptors on the GRPC stubs to catch `RESOURCE_EXHAUSTED`, sleep the specified amount of time, and then transparently retry. The result will be that the calls (eg. `client.runWf(...)`) will take longer, throttling the application but not throwing an error.

### Supporting `rpc PollTask`

The `RESOURCE_EXHAUSTED` + `RetryInfo` response requires an `onError()`, which would break a streaming RPC (eg. `rpc PollTask`). Therefore we will not throttle `rpc PollTask`, nor will `PollTaskRequest`s count against the quota.

Task workers will be throttled by the `rpc ReportTask` which is a unary grpc call. The transparent retries will delay the workers as they're currently implemented and provide a sufficient mechanism for throttling task workers.

### Delay Time Calculations (Token Bucket with Debt)

We use a **token bucket with debt** (also known as a deficit token bucket or permit borrowing) strategy. The token bucket refills at the configured rate, and its capacity equals one window's worth of permits (i.e. `rate * 500ms`). Windows refresh every 500 milliseconds (half second).

Whether a request is accepted or throttled, one permit is consumed. Throttling can drive `availablePermits` negative. This "debt" ensures that subsequent requests see the already-scheduled retries and receive proportionally longer delays.

For example, let's assume 5 requests per window and each window is 500ms. Here's a sequence of events:

1. First five requests come in and are accepted. Now I have zero permits.
2. Next five requests come in befoore the first window closes...they're all throttled 500ms. I now have **negative 5 permits.**
3. If an additional request comes in before the first window closes, it gets delayed by 1000ms. This is because I know the other requests that were already throttled are coming into the next window.

### Calculating Per-Server Throttles

The amount of requests allowed per window in each server will be calculated as `quota.writes_per_second` / `num_servers`.

### Forwarding `QuotaUsageWindow`s

Every minute, the `LHServerListener` will forward a `Command` to Kafka stating how many requests were observed, throttled, and for how long they were throttled in the past minute for each `QuotaId`. This `Command` will be aggregated per `Quota` and will be visible via the API described above.

In the case of a server crash, we will under-count the number of received requests.

## Out of Scope

### Rejected Alternatives

#### Quotas Outside `Tenants`

In many deployments of LittleHorse, users have ACL's set for specific `Tenant`s only and administrators discourage people from using `global_acls`. However, users need to be able to easily see their own quota utilization. Therefore, making the `Quota` a Tenant-scoped object saves a ton of operational friction.

The only use-case harmed by this is having a single application or person running requests in multiple `Tenant`s. However:

* A human `Principal` with access to multiple `Tenant`s is highly unlikely to exceed a quota.
* There are very few machine client use-cases in which the application accesses multiple `Tenant`s. In some of these cases, it's even desirable for the machine client to have different quotas in each `Tenant` that it accesses.

### Future Work

Future work (out of scope for this proposal) will allow:

#### Full Transparent Retries

This proposal is the first time we're taking advantage of GRPC's built-in retry capability. In the future, we'll take advantage of GRPC client interceptors to add idempotence to allow fully safe transparent retries on the client side. This will require a major refactor of the `AsyncWaiters` class but will drastically improve reliability in the face of failures.

#### Quota Rebalancing

_NOTE: This could potentially be implemented based on necessity; though it is quite possibly not needed and as such may never be implemented._

To address the problem of uneven load distribution causing artificially small quotas, we could allow each server to forward quota utilization & throttling information every minute to a central processor, and dynamically adjust

However, this is complex and is probably not very valuable and as such is out of scope for now.
